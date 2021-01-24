package com.gmail.andrewandy.regionshop.region.feature.builtins;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Objects;
import java.util.UUID;

@ConfigSerializable
public class OwnershipDataImpl {

    @Setting("owner")
    @Required
    private UUID owner;

    @Setting("override-permission")
    private String overridePermission;

    public @NotNull UUID getOwner() {
        return owner;
    }

    public void setOwner(@NotNull UUID owner) {
        this.owner = Objects.requireNonNull(owner);
    }

    public @Nullable String getOverridePermission() {
        return overridePermission;
    }

    public boolean hasOverridePermission() {
        return overridePermission != null;
    }

    public void setOverridePermission(@Nullable String overridePermission) {
        this.overridePermission = overridePermission;
    }

}
