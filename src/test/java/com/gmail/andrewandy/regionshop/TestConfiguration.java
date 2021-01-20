package com.gmail.andrewandy.regionshop;

import com.gmail.andrewandy.regionshop.configuration.DatabaseOptions;
import com.gmail.andrewandy.regionshop.configuration.InternalConfig;
import com.gmail.andrewandy.regionshop.configuration.RegionShopConfig;
import com.gmail.andrewandy.regionshop.data.DatabaseType;
import com.gmail.andrewandy.regionshop.data.JsonDataHandler;
import com.gmail.andrewandy.regionshop.data.RegionDataHandler;
import com.gmail.andrewandy.regionshop.data.SqliteDataHandler;
import com.gmail.andrewandy.regionshop.util.LogUtils;
import com.gmail.andrewandy.regionshop.util.TestModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.name.Names;
import com.zaxxer.hikari.pool.HikariPool;
import io.leangen.geantyref.TypeToken;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Level;

public class TestConfiguration {

    private static File dummyDir;

    @BeforeClass
    public static void init() {
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
    public void testConfiguration() {
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
