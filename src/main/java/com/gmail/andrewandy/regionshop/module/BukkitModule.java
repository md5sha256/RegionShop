package com.gmail.andrewandy.regionshop.module;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChainFactory;
import com.gmail.andrewandy.regionshop.RegionShopPlugin;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.logging.Logger;

public class BukkitModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RegionShopPlugin.class).toProvider(() -> RegionShopPlugin.getPlugin(RegionShopPlugin.class));
    }

    @Provides
    @Singleton
    public @NotNull TaskChainFactory provideTaskChainFactory(@NotNull RegionShopPlugin plugin) {
        return BukkitTaskChainFactory.create(plugin);
    }

    @Provides
    @Singleton
    @Named("internal-data-folder")
    public @NotNull File providePluginDirectory(@NotNull RegionShopPlugin plugin) {
        return plugin.getDataFolder();
    }

    @Provides
    @Singleton
    @Named("internal-logger")
    public @NotNull Logger providePluginLogger(@NotNull RegionShopPlugin plugin) {
        return plugin.getLogger();
    }

}
