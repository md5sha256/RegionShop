package com.github.md5sha256.regionshop.region.settings;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Supplier;

public class GroupedSettingAccessor extends BaseSettingAccessor implements SettingAccessor {

    private final Supplier<TreeSet<IRegionGroup>> groups;

    public GroupedSettingAccessor(@NotNull Supplier<TreeSet<IRegionGroup>> groups,
                                  @NotNull ConfigurationNode root) {
        super(root);
        this.groups = groups;
    }

    @Override
    public @NotNull <T> Optional<T> querySetting(@NotNull ISetting<T> setting) {
        // Query our settings first before checking the region groups
        final Optional<T> ourValue = super.querySetting(setting);
        if (ourValue.isPresent()) {
            return ourValue;
        }
        // Query the groups from highest to lowest priority
        for (final IRegionGroup group : this.groups.get()) {
            Optional<T> optional = group.settingAccessor().querySetting(setting);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }
}
