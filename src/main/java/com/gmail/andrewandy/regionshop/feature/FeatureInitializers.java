package com.gmail.andrewandy.regionshop.feature;

import com.gmail.andrewandy.regionshop.region.IRegion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class FeatureInitializers {

    private final Map<Class<?>, Initializer<?>> initializerMap = new HashMap<>();

    private @Nullable <T extends RegionFeature> Initializer<T> defaultInitializer(Class<T> clazz) {
        try {
            final Constructor<T> constructor = clazz.getConstructor(IRegion.class);
            return (region) -> {
                try {
                    return constructor.newInstance(region);
                } catch (ReflectiveOperationException ex) {
                    throw new UnsupportedOperationException(ex);
                }
            };
        } catch (ReflectiveOperationException ex) {
            return null;
        }
    }

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
