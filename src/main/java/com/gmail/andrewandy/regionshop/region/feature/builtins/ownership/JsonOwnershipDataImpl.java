package com.gmail.andrewandy.regionshop.region.feature.builtins.ownership;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.*;

@ConfigSerializable
public class JsonOwnershipDataImpl implements OwnershipData {

    @Setting("owners")
    private Set<UUID> owners = Collections.emptySet();

    @Setting("override-permission")
    private String overridePermission = null;

    public JsonOwnershipDataImpl() {

    }

    public JsonOwnershipDataImpl(@NotNull OwnershipData other) {
        this.owners = new HashSet<>(other.getOwners());
        this.overridePermission = other.getOverridePermission();
    }

    @Override
    public @NotNull Set<@NotNull UUID> getOwners() {
        return owners;
    }

    @Override
    public void setOwners(@NotNull Collection<@NotNull UUID> owners) {
        this.owners = new HashSet<>(owners);
    }

    @Override
    public boolean addOwner(@NotNull UUID owner) {
        return owners.add(owner);
    }

    @Override
    public boolean removeOwner(@NotNull UUID owner) {
        return owners.remove(owner);
    }

    @Override
    public boolean isOwner(@NotNull UUID owner) {
        return owners.contains(owner);
    }

    @Override
    public int getNumberOfOwners() {
        return owners.size();
    }

    @Override
    public @Nullable String getOverridePermission() {
        return overridePermission;
    }

    @Override
    public void setOverridePermission(@Nullable String overridePermission) {
        this.overridePermission = overridePermission;
    }

    @Override
    public boolean hasOverridePermission() {
        return overridePermission != null && !overridePermission.isEmpty();
    }

}
