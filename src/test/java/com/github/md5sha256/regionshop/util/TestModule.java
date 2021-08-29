package com.github.md5sha256.regionshop.util;

import co.aikar.taskchain.TaskChainFactory;
import com.github.md5sha256.regionshop.RegionShop;
import com.github.md5sha256.regionshop.module.RegionShopModule;
import com.google.inject.*;
import com.google.inject.name.Named;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.logging.Logger;

public class TestModule extends AbstractModule {

    public static Injector newInjector() {
        return Guice.createInjector(Stage.DEVELOPMENT, new TestModule(), new RegionShopModule(new RegionShop()));
    }

    @Provides
    @Singleton
    public @NotNull TaskChainFactory provideTaskChainFactory() {
        return new TaskChainFactory(new MockGameInterface());
    }

    @Provides
    @Singleton
    @Named("internal-data-folder")
    public @NotNull File providePluginDirectory() {
        try {
            return Files.createTempDirectory(UUID.randomUUID().toString()).toFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Provides
    @Singleton
    @Named("internal-logger")
    public @NotNull Logger providePluginLogger() {
        return Logger.getLogger("RegionShop");
    }

}
