package com.github.md5sha256.regionshop.module;

import com.github.md5sha256.regionshop.RegionShopAPI;
import com.github.md5sha256.regionshop.configuration.ConfigurationTransformer;
import com.github.md5sha256.regionshop.configuration.DatabaseOptions;
import com.github.md5sha256.regionshop.configuration.InternalConfig;
import com.github.md5sha256.regionshop.configuration.RegionShopConfig;
import com.github.md5sha256.regionshop.data.DatabaseType;
import com.github.md5sha256.regionshop.data.JsonDataHandler;
import com.github.md5sha256.regionshop.data.RegionDataHandler;
import com.github.md5sha256.regionshop.data.SqliteDataHandler;
import com.github.md5sha256.regionshop.region.RegionFactory;
import com.github.md5sha256.regionshop.region.feature.FeatureInitializers;
import com.github.md5sha256.regionshop.region.feature.FeatureManagerImpl;
import com.github.md5sha256.regionshop.region.feature.RegionFeatureManager;
import com.github.md5sha256.regionshop.region.feature.builtins.BuiltinFeatures;
import com.github.md5sha256.regionshop.region.feature.builtins.FeatureFactory;
import com.github.md5sha256.regionshop.region.feature.builtins.access.AccessDataSerializer;
import com.github.md5sha256.regionshop.region.settings.RegionGroupRegistry;
import com.github.md5sha256.regionshop.region.settings.RegionGroupRegistryImpl;
import com.github.md5sha256.regionshop.util.LogUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class RegionShopModule extends AbstractModule {

    private final RegionShopAPI api;

    public RegionShopModule(@NotNull RegionShopAPI api) {
        this.api = api;
    }

    @Override
    protected void configure() {
        bind(LogUtils.class).asEagerSingleton();
        bind(MiniMessage.class).toInstance(MiniMessage.get());
        bind(RegionShopConfig.class).to(InternalConfig.class);
        bind(AccessDataSerializer.class).asEagerSingleton();
        bind(FeatureInitializers.class).asEagerSingleton();
        bind(BuiltinFeatures.class).asEagerSingleton();
        bind(RegionGroupRegistry.class).to(RegionGroupRegistryImpl.class).in(Singleton.class);
        install(new FactoryModuleBuilder()
                .implement(RegionFeatureManager.class, FeatureManagerImpl.class)
                .build(RegionFactory.class));
        install(new FactoryModuleBuilder().build(FeatureFactory.class));
        bind(RegionShopAPI.class).toInstance(api);
    }

    @Provides
    @Singleton
    @Named("internal-config-loader")
    public @NotNull HoconConfigurationLoader provideConfigurationLoader(@Named("internal-data-folder") File dataFolder) {
        return HoconConfigurationLoader.builder().file(new File(dataFolder, "settings.conf")).build();
    }

    @Provides
    @Singleton
    @Named("internal-config")
    public @NotNull CommentedConfigurationNode provideRootConfigurationNode(@Named("internal-config-loader") HoconConfigurationLoader configurationLoader) {
        try {
            return configurationLoader.load();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Provides
    @Singleton
    @Named("region-data")
    public @NotNull ConfigurationLoader<?> provideRegionDataLoader() {
        return HoconConfigurationLoader.builder().build();
    }

    @Provides
    @Singleton
    public @NotNull InternalConfig provideInternalConfig(@Named("internal-config") CommentedConfigurationNode configurationNode, ConfigurationTransformer transformer) {
        return transformer.process(configurationNode);
    }

    @Provides
    @Singleton
    public @NotNull HikariPool provideInternalDataSource(InternalConfig config, LogUtils logUtils, @Named("internal-data-folder") File baseDir) {
        final HikariConfig hikariConfig = new HikariConfig();
        final DatabaseOptions databaseOptions = config.getDatabaseOptions();
        if (databaseOptions.getDatabaseType() == DatabaseType.JSON) {
            throw new IllegalStateException("Database does not require a connection pool!");
        }
        final String url = databaseOptions.getDatabaseURL().replace("%_", baseDir.getAbsolutePath() + File.separator);
        final String username = databaseOptions.getUsername();
        final String password = databaseOptions.getPassword();
        if (username.equalsIgnoreCase("null")) {
            logUtils.log(Level.SEVERE, "<red>Invalid database username! Username was set to null.");
            throw new IllegalStateException();
        } else if (password.equalsIgnoreCase("null")) {
            logUtils.log(Level.SEVERE, "<red>Invalid database password! Username was set to null.");
            throw new IllegalStateException();
        }
        if (databaseOptions.getDatabaseType() == DatabaseType.SQLITE) {
            if (url.endsWith("index.json")) {
                logUtils.log(Level.SEVERE, "<red>Refusing to overwrite the JsonDataHandler index file!</red>");
                throw new IllegalStateException();
            }
            final File file = new File(url);
            if (!file.isFile()) {
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    logUtils.log(Level.SEVERE, "<red>Failed to connect to the database!");
                    throw new IllegalStateException(ex);
                }
            }
            hikariConfig.setJdbcUrl("jdbc:sqlite://" + url);
        }
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setPoolName("regionshop-connection-pool");
        hikariConfig.setMaximumPoolSize(4);
        return new HikariPool(hikariConfig);
    }

    @Provides
    @Singleton
    public @NotNull RegionDataHandler provideRegionDataHandler(InternalConfig config, Injector injector) {
        final DatabaseOptions databaseOptions = config.getDatabaseOptions();
        final RegionDataHandler handler;
        switch (databaseOptions.getDatabaseType()) {
            case JSON:
                handler = new JsonDataHandler();
                break;
            case SQLITE:
                handler = new SqliteDataHandler();
                break;
            default:
                throw new IllegalStateException("Invalid DatabaseType!");
        }
        injector.injectMembers(handler);
        return handler;
    }

}
