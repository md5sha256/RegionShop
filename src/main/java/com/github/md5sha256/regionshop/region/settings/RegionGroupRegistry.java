package com.github.md5sha256.regionshop.region.settings;

import org.jetbrains.annotations.NotNull;

import java.util.NavigableSet;
import java.util.Optional;
import java.util.UUID;

public interface RegionGroupRegistry {

    @NotNull NavigableSet<IRegionGroup> groups(@NotNull UUID region);

    @NotNull Optional<IRegionGroup> getById(@NotNull String id);

    void addGroup(@NotNull IRegionGroup group);

    void removeGroup(@NotNull IRegionGroup regionGroup);

    void removeRegionFromGroup(@NotNull UUID region, @NotNull IRegionGroup regionGroup);

    @NotNull IRegionGroup createGroup(@NotNull String id);

}
