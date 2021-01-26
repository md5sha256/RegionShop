package com.gmail.andrewandy.regionshop;

import com.gmail.andrewandy.regionshop.data.RegionDataHandler;
import com.gmail.andrewandy.regionshop.region.RegionFactory;
import com.gmail.andrewandy.regionshop.region.feature.FeatureInitializers;
import org.jetbrains.annotations.NotNull;

public interface RegionShopAPI {
    @NotNull RegionDataHandler getDataHandler();

    @NotNull RegionFactory getRegionFactory();

    @NotNull FeatureInitializers getFeatureInitializers();
}
