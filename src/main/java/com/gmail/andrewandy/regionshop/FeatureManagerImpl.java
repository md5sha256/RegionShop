package com.gmail.andrewandy.regionshop;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.*;

public class FeatureManagerImpl implements RegionFeatureManager {

    @Inject
    private FeatureInitializers initializers;

    private final ClassToInstanceMap<RegionFeature> featureMap = MutableClassToInstanceMap.create();
    private final IRegion region;

    @AssistedInject
    public FeatureManagerImpl(@Assisted @NotNull IRegion region) {
        this.region = region;
    }

    @Override
    public <T extends RegionFeature> @NotNull T getOrCreateFeature(@NotNull Class<T> featureClass) throws UnsupportedOperationException {
        final T t = featureMap.getInstance(featureClass);
        if (t == null) {
            final T newInstance = initializers.newInstance(featureClass, region);
            featureMap.putInstance(featureClass, newInstance);
            return newInstance;
        }
        return t;
    }

    @Override
    public void removeFeature(@NotNull Class<? extends RegionFeature> featureClass) {
        featureMap.remove(featureClass);
    }

    @Override
    public @NotNull Set<@NotNull ? extends RegionFeature> getFeatures() {
        return new HashSet<>(featureMap.values());
    }

    @Override
    public @NotNull <T extends RegionFeature> Optional<@NotNull T> getFeature(@NotNull Class<T> featureClass) {
        return Optional.ofNullable(featureMap.getInstance(featureClass));
    }

    private void init() {

    }

}
