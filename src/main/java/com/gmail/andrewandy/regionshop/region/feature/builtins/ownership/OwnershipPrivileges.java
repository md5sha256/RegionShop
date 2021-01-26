package com.gmail.andrewandy.regionshop.region.feature.builtins.ownership;

import com.gmail.andrewandy.regionshop.region.feature.builtins.access.AccessPrivilege;
import com.gmail.andrewandy.regionshop.region.feature.builtins.access.DefaultPrivileges;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum OwnershipPrivileges implements AccessPrivilege {

    OWNER, VISITOR;

    private final String displayName = name().toLowerCase(Locale.ROOT).replaceAll("_", " ");

    @Override
    public @NotNull String displayName() {
        return displayName;
    }

    @Override
    public Integer compare(AccessPrivilege other) {
        if (other instanceof OwnershipPrivileges) {
            return this.compareTo((OwnershipPrivileges) other);
        } else if (other instanceof DefaultPrivileges){
            final DefaultPrivileges defaultPrivileges = (DefaultPrivileges) other;
            switch (defaultPrivileges) {
                case USER:
                    switch (this) {
                        case VISITOR:
                            return -1;
                        case OWNER:
                            return 0;
                    }
                case ADMIN:
                    return 1;
            }
        }
        return null;
    }
}
