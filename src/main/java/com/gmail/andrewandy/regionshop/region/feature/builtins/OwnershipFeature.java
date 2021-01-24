package com.gmail.andrewandy.regionshop.region.feature.builtins;

import com.gmail.andrewandy.regionshop.data.RegionDataHandler;
import com.gmail.andrewandy.regionshop.region.IRegion;
import com.gmail.andrewandy.regionshop.region.feature.RegionFeature;
import com.gmail.andrewandy.regionshop.util.LogUtils;
import com.google.inject.assistedinject.Assisted;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class OwnershipFeature implements RegionFeature {

    private static final String DATA_KEY = "owner-data";


    private final LogUtils logUtils;
    private final IRegion region;
    private final ConfigurationNode container;
    private OwnershipDataImpl ownershipData;
    private ConfigurationLoader<?> configurationLoader;


    public OwnershipFeature(@Assisted IRegion region, @NotNull LogUtils logUtils, @NotNull RegionDataHandler dataHandler) {
        this.logUtils = logUtils;
        this.region = region;
        this.container = region.getFeatureManager().getOrCreateDataContainer(OwnershipFeature.class);
        loadData();
    }

    public @NotNull OwnershipDataImpl newOwnershipData(@NotNull UUID owner, @Nullable String overridePermission) {
        final OwnershipDataImpl ownershipData = new OwnershipDataImpl();
        ownershipData.setOwner(owner);
        ownershipData.setOverridePermission(overridePermission);
        return ownershipData;
    }

    public @NotNull Optional<@NotNull OwnershipDataImpl> getOwnershipData() {
        return Optional.ofNullable(this.ownershipData);
    }

    public void setOwnershipData(OwnershipDataImpl ownershipData) {
        this.ownershipData = Objects.requireNonNull(ownershipData);
        Objects.requireNonNull(ownershipData.getOwner());
        Objects.requireNonNull(ownershipData.getOverridePermission());
    }

    @Override
    public void delete() {
    }

    @Override
    public void loadData() {
        final ConfigurationNode node = this.container.node(DATA_KEY);
        if (node.virtual()) {
            return;
        }
        try {
            this.ownershipData = node.get(OwnershipDataImpl.class);
        } catch (SerializationException ex) {
            logUtils.logException(ex);
            logUtils.log(Level.SEVERE, "<red>Failed to deserialize ownership data for region: " + region.getRegionID());
        }
    }

    @Override
    public boolean saveData() {
        // FIXME finish implementation
        return false;
    }
}
