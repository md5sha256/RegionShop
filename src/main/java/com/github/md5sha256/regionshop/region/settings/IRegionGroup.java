package com.github.md5sha256.regionshop.region.settings;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface IRegionGroup extends Comparable<IRegionGroup> {

    @NotNull Set<@NotNull UUID> members();

    void addMember(@NotNull UUID uuid);

    void removeMember(@NotNull UUID uuid);

    boolean isMember(@NotNull UUID uuid);

    @NotNull SettingAccessor settingAccessor();

    int priority();

    @Override
    default int compareTo(@NotNull IRegionGroup o) {
        return Integer.compare(priority(), o.priority());
    }
}
