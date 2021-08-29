package com.github.md5sha256.regionshop;

import com.github.md5sha256.regionshop.configuration.InternalConfig;
import com.github.md5sha256.regionshop.configuration.RegionShopConfig;
import com.github.md5sha256.regionshop.data.DatabaseType;
import com.github.md5sha256.regionshop.data.RegionDataHandler;
import com.github.md5sha256.regionshop.module.BukkitModule;
import com.github.md5sha256.regionshop.module.RegionShopModule;
import com.github.md5sha256.regionshop.region.RegionFactory;
import com.github.md5sha256.regionshop.region.feature.FeatureInitializers;
import com.github.md5sha256.regionshop.region.feature.builtins.BuiltinFeatures;
import com.github.md5sha256.regionshop.util.LogUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.zaxxer.hikari.pool.HikariPool;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

public class RegionShop implements RegionShopAPI {

    private LogUtils logUtils;
    private RegionShopConfig config;
    private RegionDataHandler dataHandler;
    private RegionFactory regionFactory;
    private FeatureInitializers featureInitializers;

    private Injector injector;

    public void onEnable() {
        this.injector = Guice.createInjector(Stage.PRODUCTION, new BukkitModule(), new RegionShopModule(this));

        this.logUtils = this.injector.getInstance(LogUtils.class);

        // Load setting
        this.logUtils.log(Level.INFO, "<white>[RegionShop] Loading settings...</white>");
        this.config = this.injector.getInstance(InternalConfig.class);
        this.logUtils.setPrefix(this.config.getLogPrefix());
        this.logUtils.log(Level.INFO, "<green>Settings loaded!");

        // Force data handler to be loaded early
        this.dataHandler = this.injector.getInstance(RegionDataHandler.class);
        this.logUtils.log(Level.INFO, "<green>Using " + dataHandler.getClass() + " to store region data</green>");

        final BuiltinFeatures features = this.injector.getInstance(BuiltinFeatures.class);
        features.init();

        // Setup api stuff
        this.regionFactory = this.injector.getInstance(RegionFactory.class);
        this.featureInitializers = this.injector.getInstance(FeatureInitializers.class);

        // All done!
        this.logUtils.log(Level.INFO, "<green>Plugin Enabled!</blue>");
    }

    public void onDisable() {
        this.logUtils.log(Level.INFO, "<white>Flushing cached changes...");
        try {
            this.dataHandler.flushChanges();
        } catch (IOException ex) {
            this.logUtils.logException(ex);
        }
        if (this.config.getDatabaseOptions().getDatabaseType() == DatabaseType.SQLITE) {
            // Only shutdown the pool if we are using sqlite
            this.logUtils.log(Level.INFO, "<white>Shutting down database...");
            try {
                final HikariPool pool = this.injector.getInstance(HikariPool.class);
                pool.shutdown();
            } catch (InterruptedException ex) {
                this.logUtils.logException(ex);
            }
        }
        this.logUtils.log(Level.INFO, "<green>Plugin Disabled!</blue>");
        this.logUtils = null;
        this.dataHandler = null;
        this.featureInitializers = null;
        this.regionFactory = null;
    }

    @Override
    public @NotNull RegionDataHandler getDataHandler() {
        return Objects.requireNonNull(dataHandler, "Cannot access DataHandler: Plugin is either disabled or has yet to be initialized!");
    }

    @Override
    public @NotNull RegionFactory getRegionFactory() {
        return Objects.requireNonNull(regionFactory, "Cannot access RegionFactory: Plugin is either disabled or has yet to be initialized!");
    }

    @Override
    public @NotNull FeatureInitializers getFeatureInitializers() {
        return Objects.requireNonNull(featureInitializers, "Cannot access FeatureInitializers: Plugin is either disabled or has yet to be initialized!");
    }

}
