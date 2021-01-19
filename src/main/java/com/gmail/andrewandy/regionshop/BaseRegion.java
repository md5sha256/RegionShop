package com.gmail.andrewandy.regionshop;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

public abstract class BaseRegion implements Serializable, IRegion {

    private final String world;
    private final String regionID;
    private final String displayName;
    private final RegionFeatureManager featureManager;


    protected BaseRegion(@NotNull String world, @NotNull String regionID, @NotNull String displayName,
                         @NotNull FeatureFactory featureFactory) {
        this.world = Objects.requireNonNull(world, "world cannot be null!");
        this.regionID = Objects.requireNonNull(regionID, "regionID cannot be null!");
        this.displayName = Objects.requireNonNull(displayName, "displayName cannot be null!");
        featureManager = featureFactory.newFeatureManager(this);
    }

    private transient boolean deleted;

    protected abstract void delete();

    private void markForDeletion() {
        this.deleted = true;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public String getWorld() {
        return world;
    }

    @Override
    public String getRegionID() {
        return regionID;
    }

    @Override
    public String getDisplayNameRaw() {
        return displayName;
    }

    @Override
    public RegionFeatureManager getFeatureManager() {
        return featureManager;
    }
}
