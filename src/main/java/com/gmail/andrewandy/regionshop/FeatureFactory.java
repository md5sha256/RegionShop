package com.gmail.andrewandy.regionshop;

import org.jetbrains.annotations.NotNull;

public interface FeatureFactory {

    @NotNull RegionFeatureManager newFeatureManager(@NotNull IRegion region);

}
