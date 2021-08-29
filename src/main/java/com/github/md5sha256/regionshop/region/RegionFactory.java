package com.github.md5sha256.regionshop.region;

import com.github.md5sha256.regionshop.region.feature.RegionFeatureManager;
import com.google.inject.assistedinject.Assisted;
import org.jetbrains.annotations.NotNull;

public interface RegionFactory {

    @NotNull BaseRegion newBaseRegion(@NotNull @Assisted("world") String world,
                                      @NotNull @Assisted("regionID") String regionID,
                                      @NotNull @Assisted("displayName") String displayName);

    @NotNull RegionFeatureManager newFeatureManager(@NotNull IRegion region);

}
