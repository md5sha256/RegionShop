package com.github.md5sha256.regionshop.region.feature;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.util.Optional;
import java.util.Set;

public interface RegionFeatureManager {

    @NotNull Optional<@NotNull ConfigurationNode> getDataContainer(@NotNull Class<? extends RegionFeature> featureClass);

    @NotNull ConfigurationNode getOrCreateDataContainer(@NotNull Class<? extends RegionFeature> featureClass);

    default boolean hasData(@NotNull Class<? extends RegionFeature> featureClass) {
        return getDataContainer(featureClass).isPresent();
    }

    @NotNull <T extends RegionFeature> Optional<@NotNull T> getFeature(@NotNull Class<T> featureClass);

    @NotNull <T extends RegionFeature> T getOrCreateFeature(@NotNull Class<T> featureClass) throws UnsupportedOperationException;

    default boolean isFeaturePresent(@NotNull Class<? extends RegionFeature> featureClass) {
        return getFeature(featureClass).isPresent();
    }

    @NotNull Set<@NotNull ? extends RegionFeature> getFeatures();

    void removeFeature(@NotNull Class<? extends RegionFeature> featureClass);

    void clear();

    void loadFeatures();

}
