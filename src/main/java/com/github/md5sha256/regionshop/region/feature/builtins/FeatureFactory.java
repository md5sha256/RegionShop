package com.github.md5sha256.regionshop.region.feature.builtins;

import com.github.md5sha256.regionshop.region.IRegion;
import com.github.md5sha256.regionshop.region.feature.builtins.access.JsonAccessFeatureImpl;
import com.github.md5sha256.regionshop.region.feature.builtins.ownership.JsonOwnershipFeatureImpl;
import org.jetbrains.annotations.NotNull;

public interface FeatureFactory {

    @NotNull JsonAccessFeatureImpl newJsonAccessFeature(@NotNull IRegion region);

    @NotNull JsonOwnershipFeatureImpl newJsonOwnershipFeature(@NotNull IRegion region);

}
