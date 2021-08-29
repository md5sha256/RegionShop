package com.github.md5sha256.regionshop.region.feature.builtins.access;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface AccessData {

    @NotNull Map<@NotNull UUID, @NotNull Set<AccessPrivilege>> getPrivileges();

    @NotNull Set<@NotNull ? extends AccessPrivilege> getPrivileges(@NotNull UUID user);

    void clearPrivileges(@NotNull UUID user);

    void removePrivilege(@NotNull UUID user, @NotNull AccessPrivilege... privilege);

    void addPrivilege(@NotNull UUID user, @NotNull AccessPrivilege... privilege);

    boolean hasPrivileges(@NotNull UUID user, @NotNull AccessPrivilege... min);

    boolean hasPrivilege(@NotNull UUID user, @NotNull AccessPrivilege min);

    AccessData copy();
}
