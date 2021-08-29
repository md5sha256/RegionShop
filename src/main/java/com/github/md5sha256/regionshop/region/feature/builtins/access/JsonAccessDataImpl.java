package com.github.md5sha256.regionshop.region.feature.builtins.access;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.*;

@ConfigSerializable
public class JsonAccessDataImpl implements AccessData {

    @Setting("privilege-data")
    private Map<UUID, Set<AccessPrivilege>> privilegeMap = new HashMap<>();

    public JsonAccessDataImpl() {

    }

    private JsonAccessDataImpl(@NotNull AccessData other) {
        this.privilegeMap = other.getPrivileges();
    }

    @Override
    public @NotNull Map<@NotNull UUID, @NotNull Set<AccessPrivilege>> getPrivileges() {
        final Map<UUID, Set<AccessPrivilege>> map = new HashMap<>(privilegeMap);
        for (Map.Entry<UUID, Set<AccessPrivilege>> entry : map.entrySet()) {
            entry.setValue(new HashSet<>(entry.getValue()));
        }
        return map;
    }

    @Override
    public @NotNull Set<@NotNull ? extends AccessPrivilege> getPrivileges(@NotNull UUID user) {
        final Set<? extends AccessPrivilege> privileges = privilegeMap.get(user);
        if (privileges == null) {
            return Collections.emptySet();
        }
        return new HashSet<>(privileges);
    }

    @Override
    public void clearPrivileges(@NotNull UUID user) {
        privilegeMap.remove(user);
    }

    @Override
    public void removePrivilege(@NotNull UUID user, @NotNull AccessPrivilege... privilege) {
        final Set<AccessPrivilege> privileges = privilegeMap.get(user);
        if (privileges != null) {
            privileges.removeAll(Arrays.asList(privilege));
        }
    }

    @Override
    public void addPrivilege(@NotNull UUID user, @NotNull AccessPrivilege... privilege) {
        final Set<AccessPrivilege> privileges = privilegeMap.computeIfAbsent(user, (unused) -> new HashSet<>());
        privileges.addAll(Arrays.asList(privilege));
    }

    @Override
    public boolean hasPrivileges(@NotNull UUID user, @NotNull AccessPrivilege... min) {
        final Set<AccessPrivilege> privileges = privilegeMap.get(user);
        if (privileges == null) {
            return false;
        }
        final Set<AccessPrivilege> targets = new HashSet<>(Arrays.asList(min));
        targets.removeIf(privileges::contains);
        for (AccessPrivilege privilege : targets) {
            if (!hasPrivilege(user, privilege)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasPrivilege(@NotNull UUID user, @NotNull AccessPrivilege min) {
        final Set<AccessPrivilege> privileges = privilegeMap.get(user);
        if (privileges == null) {
            return false;
        }
        // If the min is already directly registered
        if (privileges.contains(min)) {
            return true;
        }
        // Manually iterate through
        for (AccessPrivilege accessPrivilege : privileges) {
            final Integer comparison = min.compare(accessPrivilege);
            // If min is reached
            if (comparison != null && comparison >= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public JsonAccessDataImpl copy() {
        return new JsonAccessDataImpl(this);
    }
}
