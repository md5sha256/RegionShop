package com.gmail.andrewandy.regionshop.region.feature.builtins.access;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

public interface AccessPrivilege extends Serializable {

    @NotNull String displayName();

    @Nullable Integer compare(AccessPrivilege other);

}
