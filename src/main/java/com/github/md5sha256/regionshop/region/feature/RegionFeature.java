package com.github.md5sha256.regionshop.region.feature;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RegionFeature {

    void delete();

    boolean isDeleted();

    void loadData();

    boolean saveData();

    void removeData(@NotNull UUID uuid);

    default @NotNull CompletableFuture<@NotNull Boolean> saveDataAsynchronously() {
        return CompletableFuture.completedFuture(saveData());
    }

}
