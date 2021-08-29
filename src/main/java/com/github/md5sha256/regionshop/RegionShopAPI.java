package com.github.md5sha256.regionshop;

import com.github.md5sha256.regionshop.data.RegionDataHandler;
import com.github.md5sha256.regionshop.region.feature.FeatureInitializers;
import com.github.md5sha256.regionshop.region.RegionFactory;
import org.jetbrains.annotations.NotNull;

public interface RegionShopAPI {
    @NotNull RegionDataHandler getDataHandler();

    @NotNull RegionFactory getRegionFactory();

    @NotNull FeatureInitializers getFeatureInitializers();
}
