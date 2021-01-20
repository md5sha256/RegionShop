package com.gmail.andrewandy.regionshop.configuration;

import org.jetbrains.annotations.NotNull;

public interface RegionShopConfig {

    int getConfigVersion();

    @NotNull String getLogPrefix();

    @NotNull DatabaseOptions getDatabaseOptions();
}
