package com.gmail.andrewandy.regionshop.region;

import com.gmail.andrewandy.regionshop.region.feature.RegionFeature;
import com.gmail.andrewandy.regionshop.region.feature.RegionFeatureManager;
import com.gmail.andrewandy.regionshop.region.settings.GroupedSettingAccessor;
import com.gmail.andrewandy.regionshop.region.settings.RegionGroupRegistry;
import com.gmail.andrewandy.regionshop.region.settings.SettingAccessor;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BaseRegion implements Serializable, IRegion {

    private final UUID uuid;
    private final String world;
    private final String regionID;
    private final String displayName;
    private transient final RegionFeatureManager featureManager;
    private transient final RegionGroupRegistry groupRegistry;
    private transient final SettingAccessor settingAccessor;

    private transient CompletableFuture<Void> deletionTask;
    private transient boolean deleted;

    @AssistedInject
    public BaseRegion(@NotNull @Assisted("world") String world,
                      @NotNull @Assisted("regionID") String regionID,
                      @NotNull @Assisted("displayName") String displayName,
                      @NotNull RegionFactory regionFactory,
                      @NotNull RegionGroupRegistry regionGroupRegistry,
                      @NotNull ConfigurationLoader<?> configurationLoader
    ) {
        this(UUID.randomUUID(), world, regionID, displayName, regionFactory, regionGroupRegistry, configurationLoader);
    }

    @AssistedInject
    public BaseRegion(@NotNull @Assisted UUID uuid,
                      @NotNull @Assisted("world") String world,
                      @NotNull @Assisted("regionID") String regionID,
                      @NotNull @Assisted("displayName") String displayName,
                      @NotNull RegionFactory regionFactory,
                      @NotNull RegionGroupRegistry regionGroupRegistry,
                      @NotNull ConfigurationLoader<?> configurationLoader
                      ) {

        this.uuid = Objects.requireNonNull(uuid);
        this.world = Objects.requireNonNull(world, "world cannot be null!");
        this.regionID = Objects.requireNonNull(regionID, "regionID cannot be null!");
        this.displayName = Objects.requireNonNull(displayName, "displayName cannot be null!");
        this.featureManager = regionFactory.newFeatureManager(this);
        this.groupRegistry = regionGroupRegistry;
        this.settingAccessor = new GroupedSettingAccessor(() -> regionGroupRegistry.groups(this.uuid), configurationLoader.createNode());
    }

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
    public @NotNull UUID uuid() {
        return this.uuid;
    }

    @Override
    public boolean deleted() {
        return this.deleted;
    }

    @Override
    public @NotNull String world() {
        return this.world;
    }

    @Override
    public @NotNull String regionId() {
        return this.regionID;
    }

    @Override
    public @NotNull String displayNameRaw() {
        return this.displayName;
    }

    @Override
    public @NotNull RegionFeatureManager featureManager() {
        return this.featureManager;
    }

    @Override
    public @NotNull SettingAccessor settingAccessor() {
        return this.settingAccessor;
    }
}
