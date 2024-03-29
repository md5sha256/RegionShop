package com.github.md5sha256.regionshop.configuration;

import org.jetbrains.annotations.NotNull;

public interface RegionShopConfig {

    int getConfigVersion();

    @NotNull String getLogPrefix();

    void setLogPrefix(@NotNull String prefix);

    @NotNull DatabaseOptions getDatabaseOptions();

    void setDatabaseOption(@NotNull DatabaseOptions databaseOptions);
}
