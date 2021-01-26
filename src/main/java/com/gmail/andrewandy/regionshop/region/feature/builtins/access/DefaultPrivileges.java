package com.gmail.andrewandy.regionshop.region.feature.builtins.access;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum DefaultPrivileges implements AccessPrivilege {

    ADMIN, USER;

    private final String displayName = name().toLowerCase(Locale.ROOT).replaceAll("_", " ");

    @Override
    public @NotNull String displayName() {
        return displayName;
    }

    @Override
    public @Nullable Integer compare(AccessPrivilege other) {
        if (other == this) {
            return 0;
        }
        if (other instanceof DefaultPrivileges) {
            switch ((DefaultPrivileges) other) {
                case ADMIN:
                    if (this == USER) {
                        return -1;
                    }
                case USER:
                    if (this == ADMIN) {
                        return 1;
                    }
            }
        }
        return null;
    }
}
