package com.gmail.andrewandy.regionshop.region;

import com.gmail.andrewandy.regionshop.region.feature.RegionFeature;
import com.gmail.andrewandy.regionshop.region.feature.RegionFeatureManager;
import com.google.inject.assistedinject.Assisted;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BaseRegion implements Serializable, IRegion {

    private final UUID uuid;
    private final String world;
    private final String regionID;
    private final String displayName;
    private final RegionFeatureManager featureManager;
    private transient CompletableFuture<Void> deletionTask;
    private transient boolean deleted;

    public BaseRegion(@NotNull @Assisted("world") String world,
                      @NotNull @Assisted("regionID") String regionID,
                      @NotNull @Assisted("displayName") String displayName,
                      @NotNull RegionFactory featureFactory) {

        this(UUID.randomUUID(), world, regionID, displayName, featureFactory);
    }

    public BaseRegion(@NotNull @Assisted UUID uuid,
                      @NotNull @Assisted("world") String world,
                      @NotNull @Assisted("regionID") String regionID,
                      @NotNull @Assisted("displayName") String displayName,
                      @NotNull RegionFactory regionFactory) {

        this.uuid = Objects.requireNonNull(uuid);
        this.world = Objects.requireNonNull(world, "world cannot be null!");
        this.regionID = Objects.requireNonNull(regionID, "regionID cannot be null!");
        this.displayName = Objects.requireNonNull(displayName, "displayName cannot be null!");
        featureManager = regionFactory.newFeatureManager(this);
    }

    protected void delete() {
        for (RegionFeature regionFeature : featureManager.getFeatures()) {
            regionFeature.delete();
        }
        featureManager.clear();
    }

    @Override
    public @NotNull
    synchronized CompletableFuture<Void> queueDeletion() {
        if (this.deleted) {
            return this.deletionTask;
        }
        this.deleted = true;
        delete();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull UUID getUUID() {
        return this.uuid;
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
