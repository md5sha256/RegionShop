package com.gmail.andrewandy.regionshop.region;

import com.gmail.andrewandy.regionshop.region.feature.RegionFeatureManager;
import com.google.inject.assistedinject.Assisted;
import org.jetbrains.annotations.NotNull;

public interface RegionFactory {

    @NotNull BaseRegion newBaseRegion(@NotNull @Assisted("world") String world,
                                      @NotNull @Assisted("regionID") String regionID,
                                      @NotNull @Assisted("displayName") String displayName);

    @NotNull RegionFeatureManager newFeatureManager(@NotNull IRegion region);

}
