package com.github.md5sha256.regionshop.region.feature.builtins.ownership;

import com.github.md5sha256.regionshop.region.feature.RegionFeature;
import com.github.md5sha256.regionshop.util.LogUtils;
import com.github.md5sha256.regionshop.region.IRegion;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public final class JsonOwnershipFeatureImpl implements RegionFeature {

    private static final String DATA_KEY = "owner-data";

    private final LogUtils logUtils;
    private final IRegion region;
    private final ConfigurationNode container;
    private boolean deleted;
    private OwnershipData ownershipData;


    @AssistedInject
    public JsonOwnershipFeatureImpl(@Assisted IRegion region, @NotNull LogUtils logUtils) {
        this.logUtils = logUtils;
        this.region = region;
        this.container = region.featureManager().getOrCreateDataContainer(JsonOwnershipFeatureImpl.class);
        loadData();
    }


    public @NotNull Optional<@NotNull ? extends OwnershipData> getOwnershipData() {
        return Optional.ofNullable(this.ownershipData);
    }

    public @NotNull OwnershipData getOrCreateOwnershipData() {
        if (this.ownershipData == null) {
            this.ownershipData = new JsonOwnershipDataImpl();
        }
        return this.ownershipData;
    }

    public void offerData(@NotNull UUID user, @NotNull OwnershipData ownershipData) {
        this.ownershipData = ownershipData;
    }

    @Override
    public boolean isDeleted() {
        return this.deleted;
    }

    @Override
    public void delete() {
        if (this.deleted) {
            return;
        }
        this.deleted = true;
        this.ownershipData = null;
        this.container.removeChild(DATA_KEY);
    }

    @Override
    public void loadData() {
        final ConfigurationNode node = this.container.node(DATA_KEY);
        if (node.virtual()) {
            return;
        }
        try {
            this.ownershipData = node.get(JsonOwnershipDataImpl.class);
        } catch (SerializationException ex) {
            logUtils.logException(ex);
            logUtils.log(Level.SEVERE, "<red>Failed to deserialize ownership data for region: " + region.regionId());
        }
    }

    @Override
    public void removeData(@NotNull UUID uuid) {
        this.ownershipData.removeOwner(uuid);
    }

    @Override
    public boolean saveData() {
        final ConfigurationNode node = this.container.node(DATA_KEY);
        try {
            node.set(this.ownershipData);
            return true;
        } catch (SerializationException ex) {
            logUtils.logException(ex);
            logUtils.log(Level.WARNING, "<yellow>Failed to save ownership data for region: " + region.regionId());
            return false;
        }
    }

}
