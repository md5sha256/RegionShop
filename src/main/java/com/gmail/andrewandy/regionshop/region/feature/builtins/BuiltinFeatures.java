package com.gmail.andrewandy.regionshop.region.feature.builtins;

import com.gmail.andrewandy.regionshop.data.RegionDataHandler;
import com.gmail.andrewandy.regionshop.region.feature.FeatureInitializers;
import com.gmail.andrewandy.regionshop.region.feature.FeatureManagerImpl;
import com.gmail.andrewandy.regionshop.region.feature.builtins.access.JsonAccessFeatureImpl;
import com.gmail.andrewandy.regionshop.region.feature.builtins.ownership.JsonOwnershipFeatureImpl;
import com.google.inject.Inject;

public class BuiltinFeatures {

    @Inject
    private FeatureManagerImpl featureManager;
    @Inject
    private FeatureInitializers initializers;
    @Inject
    private FeatureFactory featureFactory;
    @Inject
    private RegionDataHandler dataHandler;


    public void init() {
        initFeatureOwnership();
        initFeatureAccess();
    }

    private void initFeatureAccess() {
        initializers.registerInitializer(JsonAccessFeatureImpl.class, featureFactory::newJsonAccessFeature);
        // FIXME register serializer which wraps AccessPrivilege classes.
    }

    private void initFeatureOwnership() {
        initializers.registerInitializer(JsonOwnershipFeatureImpl.class, featureFactory::newJsonOwnershipFeature);
    }

}
