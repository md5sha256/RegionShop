package com.github.md5sha256.regionshop.region.feature.builtins.ownership;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface OwnershipData {

    @NotNull Set<@NotNull UUID> getOwners();

    void setOwners(@NotNull Collection<@NotNull UUID> owners);

    boolean addOwner(@NotNull UUID owner);

    boolean removeOwner(@NotNull UUID owner);

    boolean isOwner(@NotNull UUID owner);

    @Range(from = 0L, to = Long.MAX_VALUE)
    int getNumberOfOwners();

    @Nullable String getOverridePermission();

    void setOverridePermission(@Nullable String overridePermission);

    boolean hasOverridePermission();

}
