package com.gmail.andrewandy.regionshop.region.settings;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

public interface RegionGroupRegistry {

    @NotNull TreeSet<IRegionGroup> groups(@NotNull UUID region);

    @NotNull Optional<IRegionGroup> getById(@NotNull String id);

    void addGroup(@NotNull IRegionGroup group);

    void removeGroup(@NotNull IRegionGroup regionGroup);

    void removeRegionFromGroup(@NotNull UUID region);

}
