package com.github.md5sha256.regionshop.region.settings;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class RegionGroupRegistryImpl implements RegionGroupRegistry {

    private final Map<UUID, NavigableSet<IRegionGroup>> uuidGroupsMap = new HashMap<>();
    private final Map<String, IRegionGroup> idGroupMap = new HashMap<>();

    @Inject
    @Named("region-data")
    private ConfigurationLoader<?> configurationLoader;

    @Override
    public @NotNull NavigableSet<IRegionGroup> groups(@NotNull UUID region) {
        return this.uuidGroupsMap.getOrDefault(region, Collections.emptyNavigableSet());
    }

    @Override
    public @NotNull Optional<IRegionGroup> getById(@NotNull String id) {
        return Optional.ofNullable(this.idGroupMap.get(id));
    }

    @Override
    public void addGroup(@NotNull IRegionGroup group) {
        removeGroup(group);
        this.idGroupMap.put(group.id(), group);
        for (UUID region : group.members()) {
            this.uuidGroupsMap.computeIfAbsent(region, x -> new TreeSet<>()).add(group);
        }
    }

    @Override
    public void removeGroup(@NotNull IRegionGroup regionGroup) {
        this.idGroupMap.remove(regionGroup.id());
        for (UUID region : regionGroup.members()) {
            NavigableSet<IRegionGroup> groups = this.uuidGroupsMap.get(region);
            if (groups != null) {
                groups.remove(regionGroup);
                if (groups.isEmpty()) {
                    // Clean up references
                    this.uuidGroupsMap.remove(region);
                }
            }
        }
    }

    @Override
    public void removeRegionFromGroup(@NotNull UUID region, @NotNull IRegionGroup regionGroup) {
        regionGroup.removeMember(region);
    }

    @Override
    public @NotNull IRegionGroup createGroup(@NotNull String id) {
        return new TrackedRegionGroup(id, this.configurationLoader.createNode());
    }

    private class TrackedRegionGroup implements IRegionGroup {

        private final String id;
        private final Set<UUID> members;
        private final ConfigurationNode root;

        private TrackedRegionGroup(@NotNull String id, @NotNull ConfigurationNode configurationNode) {
            this.id = id;
            this.root = configurationNode;
            this.members = new HashSet<>();
        }

        @Override
        public @NotNull String id() {
            return this.id;
        }

        @Override
        public @NotNull Set<@NotNull UUID> members() {
            return Collections.unmodifiableSet(this.members);
        }

        @Override
        public void addMember(@NotNull UUID uuid) {
            if (this.members.add(uuid)) {
                RegionGroupRegistryImpl.this.uuidGroupsMap.computeIfAbsent(uuid,
                        x -> new TreeSet<>()).add(this);
            }
        }

        @Override
        public void removeMember(@NotNull UUID uuid) {
            if (!this.members.remove(uuid)) {
                // Nothing changed
                return;
            }
            NavigableSet<IRegionGroup> groups = RegionGroupRegistryImpl.this.uuidGroupsMap.get(uuid);
            if (groups != null) {
                groups.remove(this);
                if (groups.isEmpty()) {
                    RegionGroupRegistryImpl.this.uuidGroupsMap.remove(uuid);
                }
            }
        }

        @Override
        public boolean isMember(@NotNull UUID uuid) {
            return this.members.contains(uuid);
        }

        @Override
        public @NotNull SettingAccessor settingAccessor() {
            return new BaseSettingAccessor(this.root);
        }

        @Override
        public int priority() {
            return 0;
        }
    }
}
