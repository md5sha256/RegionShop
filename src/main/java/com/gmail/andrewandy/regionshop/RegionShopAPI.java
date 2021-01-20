package com.gmail.andrewandy.regionshop;

import com.gmail.andrewandy.regionshop.data.RegionDataHandler;
import com.gmail.andrewandy.regionshop.feature.FeatureFactory;
import com.gmail.andrewandy.regionshop.feature.FeatureInitializers;
import org.jetbrains.annotations.NotNull;

public interface RegionShopAPI {
    @NotNull RegionDataHandler getDataHandler();

    @NotNull FeatureFactory getFeatureFactory();

    @NotNull FeatureInitializers getFeatureInitializers();
}
