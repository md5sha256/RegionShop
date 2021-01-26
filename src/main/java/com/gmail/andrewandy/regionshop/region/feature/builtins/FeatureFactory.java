package com.gmail.andrewandy.regionshop.region.feature.builtins;

import com.gmail.andrewandy.regionshop.region.IRegion;
import com.gmail.andrewandy.regionshop.region.feature.builtins.access.JsonAccessFeatureImpl;
import com.gmail.andrewandy.regionshop.region.feature.builtins.ownership.JsonOwnershipFeatureImpl;
import org.jetbrains.annotations.NotNull;

public interface FeatureFactory {

    @NotNull JsonAccessFeatureImpl newJsonAccessFeature(@NotNull IRegion region);

    @NotNull JsonOwnershipFeatureImpl newJsonOwnershipFeature(@NotNull IRegion region);

}
