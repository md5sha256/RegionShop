package com.gmail.andrewandy.regionshop.region;

import com.gmail.andrewandy.regionshop.region.feature.RegionFeatureManager;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IRegion {

    @NotNull UUID getUUID();

    boolean isDeleted();

    @NotNull CompletableFuture<Void> queueDeletion();

    @NotNull String getWorld();

    @NotNull String getRegionID();

    @NotNull String getDisplayNameRaw();

    @NotNull RegionFeatureManager getFeatureManager();

}
