package com.gmail.andrewandy.regionshop.region;

import com.gmail.andrewandy.regionshop.feature.FeatureFactory;
import com.gmail.andrewandy.regionshop.feature.RegionFeature;
import com.gmail.andrewandy.regionshop.feature.RegionFeatureManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class BaseRegion implements Serializable, IRegion {

    private final String world;
    private final String regionID;
    private final String displayName;
    private final RegionFeatureManager featureManager;
    private transient CompletableFuture<Void> deletionTask;


    protected BaseRegion(@NotNull String world, @NotNull String regionID, @NotNull String displayName,
                         @NotNull FeatureFactory featureFactory) {
        this.world = Objects.requireNonNull(world, "world cannot be null!");
        this.regionID = Objects.requireNonNull(regionID, "regionID cannot be null!");
        this.displayName = Objects.requireNonNull(displayName, "displayName cannot be null!");
        featureManager = featureFactory.newFeatureManager(this);
    }

    private transient boolean deleted;

    protected void delete() {
        for (RegionFeature regionFeature : featureManager.getFeatures()) {
            regionFeature.delete();
        }
        featureManager.clear();
    }

    @Override
    public @NotNull synchronized CompletableFuture<Void> queueDeletion() {
        if (this.deleted) {
            return this.deletionTask;
        }
        this.deleted = true;
        delete();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public @NotNull String getWorld() {
        return world;
    }

    @Override
    public @NotNull String getRegionID() {
        return regionID;
    }

    @Override
    public @NotNull String getDisplayNameRaw() {
        return displayName;
    }

    @Override
    public @NotNull RegionFeatureManager getFeatureManager() {
        return featureManager;
    }
}
