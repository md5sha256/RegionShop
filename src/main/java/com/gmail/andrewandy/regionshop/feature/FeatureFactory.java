package com.gmail.andrewandy.regionshop.feature;

import com.gmail.andrewandy.regionshop.region.IRegion;
import org.jetbrains.annotations.NotNull;

public interface FeatureFactory {

    @NotNull RegionFeatureManager newFeatureManager(@NotNull IRegion region);

}
