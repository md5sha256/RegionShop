package com.gmail.andrewandy.regionshop.region.feature;

import com.gmail.andrewandy.regionshop.region.IRegion;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FeatureInitializers {

    private final Map<Class<?>, Initializer<?>> initializerMap = new HashMap<>();

    public <T extends RegionFeature> void registerInitializer(@NotNull Class<T> clazz, @NotNull Initializer<T> initializer) {
        initializerMap.put(clazz, initializer);
    }

    public <T extends RegionFeature> @NotNull T newInstance(@NotNull Class<T> clazz, @NotNull IRegion region)
            throws UnsupportedOperationException {
        @SuppressWarnings("rawtypes")
        final Initializer initializer = initializerMap.get(clazz);
        if (initializer == null) {
            throw new UnsupportedOperationException("No initializer present for class: " + clazz.getCanonicalName());
        }
        return clazz.cast(initializer.newInstance(region));
    }

    @FunctionalInterface
    private interface Initializer<T extends RegionFeature> {

        @NotNull T newInstance(@NotNull IRegion region);

    }

}
