package com.github.md5sha256.regionshop.region.feature;

import com.github.md5sha256.regionshop.data.RegionDataHandler;
import com.github.md5sha256.regionshop.region.IRegion;
import com.github.md5sha256.regionshop.util.LogUtils;
import com.google.common.collect.MutableClassToInstanceMap;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.leangen.geantyref.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

public class FeatureManagerImpl implements RegionFeatureManager {

    private static final TypeToken<MutableClassToInstanceMap<RegionFeature>> MAP_TOKEN =
            new TypeToken<>() {
            };
    private final MutableClassToInstanceMap<RegionFeature> featureMap = MutableClassToInstanceMap.create();
    private final IRegion region;

    private final RegionDataHandler dataHandler;

    @Inject
    private FeatureInitializers initializers;
    @Inject
    private LogUtils logUtils;

    @AssistedInject
    public FeatureManagerImpl(@Assisted @NotNull IRegion region, @NotNull RegionDataHandler dataHandler) {
        this.region = region;
        this.dataHandler = dataHandler;
    }

    private static String getKeyFor(@NotNull Class<?> clazz) {
        return clazz.getCanonicalName();
    }

    @Override
    public @NotNull ConfigurationNode getOrCreateDataContainer(@NotNull Class<? extends RegionFeature> featureClass) {
        final ConfigurationNode rootNode =  dataHandler.getOrCreateDataFor(region);
        return rootNode.node(getKeyFor(featureClass));
    }

    @Override
    public @NotNull Optional<@NotNull ConfigurationNode> getDataContainer(@NotNull Class<? extends RegionFeature> featureClass) {
        final Optional<? extends ConfigurationNode> root = dataHandler.getDataFor(region);
        if (!root.isPresent()) {
            return Optional.empty();
        }
        final ConfigurationNode rootNode = root.get();
        final ConfigurationNode sub = rootNode.node(getKeyFor(featureClass));
        return sub.virtual() ? Optional.empty() : Optional.of(sub);
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
            this.logUtils.log(Level.SEVERE, "<red>Failed to deserialize feature data for region: " + region.regionId());
        }
    }
}
