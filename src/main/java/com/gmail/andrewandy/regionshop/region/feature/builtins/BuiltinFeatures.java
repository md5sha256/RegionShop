package com.gmail.andrewandy.regionshop.region.feature.builtins;

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


    public void init() {
        initFeatureOwnership();
        initFeatureAccess();
    }

    private void initFeatureAccess() {
        initializers.registerInitializer(JsonAccessFeatureImpl.class, featureFactory::newJsonAccessFeature);

    }

    private void initFeatureOwnership() {
        initializers.registerInitializer(JsonOwnershipFeatureImpl.class, featureFactory::newJsonOwnershipFeature);
    }

}
