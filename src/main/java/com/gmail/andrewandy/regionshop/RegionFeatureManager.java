package com.gmail.andrewandy.regionshop;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface RegionFeatureManager {

    @NotNull <T extends RegionFeature> Optional<@NotNull T> getFeature(@NotNull Class<T> featureClass);

    @NotNull <T extends RegionFeature> T getOrCreateFeature(@NotNull Class<T> featureClass) throws UnsupportedOperationException;

    default boolean isFeaturePresent(@NotNull Class<? extends RegionFeature> featureClass) {
        return getFeature(featureClass).isPresent();
    }

    @NotNull Set<@NotNull ? extends RegionFeature> getFeatures();

    void removeFeature(@NotNull Class<? extends RegionFeature> featureClass);

}
