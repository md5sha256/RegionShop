package com.github.md5sha256.regionshop.region.feature.builtins;

import com.github.md5sha256.regionshop.region.feature.FeatureInitializers;
import com.github.md5sha256.regionshop.region.feature.builtins.access.AccessDataSerializer;
import com.github.md5sha256.regionshop.data.RegionDataHandler;
import com.github.md5sha256.regionshop.region.feature.builtins.access.AccessData;
import com.github.md5sha256.regionshop.region.feature.builtins.access.JsonAccessFeatureImpl;
import com.github.md5sha256.regionshop.region.feature.builtins.ownership.JsonOwnershipFeatureImpl;
import com.google.inject.Inject;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

public class BuiltinFeatures {

    @Inject
    private FeatureInitializers initializers;
    @Inject
    private FeatureFactory featureFactory;
    @Inject
    private RegionDataHandler dataHandler;
    @Inject
    private AccessDataSerializer accessDataSerializer;


    public void init() {
        // Initialize serialization first
        initSerialization();

        // Init features
        initFeatureOwnership();
        initFeatureOwnership();
    }

    private void initSerialization() {
        dataHandler.addTypeSerializers(TypeSerializerCollection.builder().register(AccessData.class, accessDataSerializer).build());
    }

    private void initFeatureAccess() {
        initializers.registerInitializer(JsonAccessFeatureImpl.class, featureFactory::newJsonAccessFeature);
    }

    private void initFeatureOwnership() {
        initializers.registerInitializer(JsonOwnershipFeatureImpl.class, featureFactory::newJsonOwnershipFeature);
    }

}
