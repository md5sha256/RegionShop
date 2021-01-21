package com.gmail.andrewandy.regionshop.feature;

import com.gmail.andrewandy.regionshop.data.RegionDataHandler;
import com.gmail.andrewandy.regionshop.region.IRegion;
import com.gmail.andrewandy.regionshop.util.LogUtils;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import javax.inject.Inject;
import java.util.*;
import java.util.logging.Level;

public class FeatureManagerImpl implements RegionFeatureManager {

    private static final TypeToken<MutableClassToInstanceMap<RegionFeature>> MAP_TOKEN =
            new TypeToken<MutableClassToInstanceMap<RegionFeature>>() {
            };

    @Inject
    private FeatureInitializers initializers;
    @Inject
    private RegionDataHandler dataHandler;
    @Inject
    private LogUtils logUtils;

    private final MutableClassToInstanceMap<RegionFeature> featureMap = MutableClassToInstanceMap.create();
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

    @Override
    public void clear() {
        this.featureMap.clear();
    }

    @Override
    public void loadFeatures() {
        this.featureMap.clear();
        final ConfigurationNode data = dataHandler.getOrCreateDataFor(region);
        if (data.empty()) {
            return;
        }
        try {
            final MutableClassToInstanceMap<RegionFeature> map = data.get(MAP_TOKEN);
            if (map != null) {
                this.featureMap.putAll(map);
            }
        } catch (SerializationException ex) {
            this.logUtils.logException(ex);
            this.logUtils.log(Level.SEVERE, "<red>Failed to deserialize feature data for region: " + region.getRegionID());
        }
    }
}
