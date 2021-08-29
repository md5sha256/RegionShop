package com.github.md5sha256.regionshop.tests;

import com.github.md5sha256.regionshop.configuration.ConfigurationTransformer;
import com.github.md5sha256.regionshop.configuration.DatabaseOptions;
import com.github.md5sha256.regionshop.configuration.InternalConfig;
import com.github.md5sha256.regionshop.configuration.RegionShopConfig;
import com.github.md5sha256.regionshop.data.DatabaseType;
import com.github.md5sha256.regionshop.data.JsonDataHandler;
import com.github.md5sha256.regionshop.data.RegionDataHandler;
import com.github.md5sha256.regionshop.data.SqliteDataHandler;
import com.github.md5sha256.regionshop.util.LogUtils;
import com.github.md5sha256.regionshop.util.TestModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.name.Names;
import com.zaxxer.hikari.pool.HikariPool;
import io.leangen.geantyref.TypeToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Level;

public class TestConfiguration {

    private static File dummyDir;

    @BeforeAll
    public static void load() {
        try {
            dummyDir = Files.createTempDirectory(UUID.randomUUID().toString()).toFile();
            Class.forName("org.sqlite.SQLiteDataSource");
        } catch (IOException | ClassNotFoundException ex) {
            Assertions.fail(ex);
        }
    }

    private static void handleShutdown(Injector injector) {
        final LogUtils logUtils = injector.getInstance(LogUtils.class);
        final RegionShopConfig config = injector.getInstance(RegionShopConfig.class);
        try {
            final RegionDataHandler dataHandler = injector.getInstance(RegionDataHandler.class);
            try {
                dataHandler.flushChanges();
            } catch (IOException ex) {
                logUtils.logException(ex);
            }
        } catch (ProvisionException ex) {
            return;
        }
        if (config.getDatabaseOptions().getDatabaseType() == DatabaseType.SQLITE) {
            // Only shutdown the pool if we are using sqlite
            logUtils.log(Level.INFO, "<white>Shutting down database...");
            try {
                try {
                    final HikariPool pool = injector.getInstance(HikariPool.class);
                    pool.shutdown();
                } catch (ProvisionException ignored) {

                }
            } catch (InterruptedException ex) {
                logUtils.logException(ex);
            }
        }
    }
    
    @Test
    public void testConfigurationMutation() {
        final HoconConfigurationLoader loader = HoconConfigurationLoader.builder().build();
        final ConfigurationNode initialNode = loader.createNode();
        final InternalConfig initialConfig = new InternalConfig();
        Assertions.assertEquals(initialConfig, new InternalConfig());
        final DatabaseOptions fromConfig = initialConfig.getDatabaseOptions();
        Assertions.assertEquals(fromConfig, DatabaseOptions.defaultOptions());
        final String randomName = UUID.randomUUID().toString();
        fromConfig.setUsername(randomName);
        fromConfig.setPassword(randomName);
        fromConfig.setDatabaseURL("abc");
        fromConfig.setDatabaseType(DatabaseType.SQLITE);
        Assertions.assertEquals(fromConfig.getUsername(), randomName);
        Assertions.assertEquals(fromConfig.getPassword(), randomName);
        Assertions.assertEquals(fromConfig.getDatabaseURL(), "abc");
        Assertions.assertEquals(fromConfig.getDatabaseType(), DatabaseType.SQLITE);
        try {
            initialNode.set(initialConfig);
            final InternalConfig deserialized = initialNode.get(TypeToken.get(InternalConfig.class));
            Assertions.assertNotNull(deserialized);
            Assertions.assertEquals(deserialized, initialConfig);
            Assertions.assertEquals(deserialized.hashCode(), initialConfig.hashCode());
        } catch (SerializationException ex) {
            Assertions.fail(ex);
        }
        final InternalConfig empty = new InternalConfig();
        empty.setValues(initialConfig);
        Assertions.assertEquals(empty, initialConfig);
    }

    @Test
    public void testConfigurationLoading() {
        final File file = new File(dummyDir, "settings.conf");
        final HoconConfigurationLoader configurationLoader = HoconConfigurationLoader.builder().file(file).build();
        Assertions.assertTrue(configurationLoader.canLoad());
        Assertions.assertTrue(configurationLoader.canSave());
        final ConfigurationNode node;
        try {
            node = configurationLoader.load();
            Assertions.assertTrue(node.empty());
            Assertions.assertNull(node.get(TypeToken.get(InternalConfig.class)));
        } catch (ConfigurateException ex) {
            Assertions.fail(ex);
            return;
        }
        final InternalConfig def = new InternalConfig();
        try {
            node.set(def);
            configurationLoader.save(node);
            final HoconConfigurationLoader newLoader = HoconConfigurationLoader.builder().file(file).build();
            final ConfigurationNode loaded = newLoader.load();
            Assertions.assertNotNull(loaded.get(TypeToken.get(InternalConfig.class)));
        } catch (ConfigurateException ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    public void testConfigVersionValidation() {
        Injector injector = TestModule.newInjector();
        CommentedConfigurationNode node = injector.getInstance(Key.get(CommentedConfigurationNode.class, Names.named("internal-config")));
        final InternalConfig config = new InternalConfig();
        try {
            final Field field = InternalConfig.class.getDeclaredField("configVersion");
            field.setAccessible(true);
            field.setInt(config, ConfigurationTransformer.LATEST_VERSION + 1);
        } catch (ReflectiveOperationException ex) {
            Assertions.fail(ex);
        }
        try {
            node.set(config);
        } catch (ConfigurateException ex) {
            Assertions.fail(ex);
        }
        Assertions.assertThrows(ProvisionException.class, () -> injector.getInstance(InternalConfig.class));
    }


    @Test
    public void testSqliteDatabaseConfigurationValidation() {
        Injector injector = TestModule.newInjector();
        InternalConfig config = injector.getInstance(InternalConfig.class);
        DatabaseOptions databaseOptions = config.getDatabaseOptions();
        File baseDir = injector.getInstance(Key.get(File.class, Names.named("internal-data-folder")));
        new File(baseDir, "data").mkdir();
        databaseOptions.setDatabaseType(DatabaseType.SQLITE);
        Assertions.assertThrows(ProvisionException.class, () -> injector.getInstance(RegionDataHandler.class));
        handleShutdown(injector);
    }

    @Test
    public void testSqliteDatabaseConfiguration() {
        Injector injector = TestModule.newInjector();
        InternalConfig config = injector.getInstance(InternalConfig.class);
        DatabaseOptions databaseOptions = config.getDatabaseOptions();
        databaseOptions.setDatabaseType(DatabaseType.SQLITE);
        File baseDir = injector.getInstance(Key.get(File.class, Names.named("internal-data-folder")));
        new File(baseDir, "data").mkdir();
        databaseOptions.setUsername("username");
        databaseOptions.setPassword("password");
        databaseOptions.setDatabaseURL("%_data" + File.separator + "data.db");
        final RegionDataHandler dataHandler = injector.getInstance(RegionDataHandler.class);
        try {
            dataHandler.init();
            dataHandler.flushChanges();
        } catch (IOException ex) {
            Assertions.fail(ex);
        }
        Assertions.assertTrue(dataHandler instanceof SqliteDataHandler);
    }

    @Test
    public void testJsonDatabaseConfiguration() {
        final Injector injector = TestModule.newInjector();
        final InternalConfig config = injector.getInstance(InternalConfig.class);
        final DatabaseOptions databaseOptions = config.getDatabaseOptions();
        Assertions.assertEquals(databaseOptions.getDatabaseType(), DatabaseType.JSON);
        final RegionDataHandler dataHandler = injector.getInstance(RegionDataHandler.class);
        Assertions.assertTrue(dataHandler instanceof JsonDataHandler);
        handleShutdown(injector);
    }

}
