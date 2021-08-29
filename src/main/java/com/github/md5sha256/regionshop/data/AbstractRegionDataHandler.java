package com.github.md5sha256.regionshop.data;

import com.github.md5sha256.regionshop.region.IRegion;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractRegionDataHandler implements RegionDataHandler {

    protected final Map<@NotNull UUID, @NotNull ConfigurationNode> cachedData = new ConcurrentHashMap<>();

    private volatile TypeSerializerCollection serializers = TypeSerializerCollection.defaults();

    @Override
    public synchronized void addTypeSerializers(@NotNull TypeSerializerCollection collection) {
        this.serializers = collection.childBuilder().registerAll(collection).build();
    }

    @Override
    public @NotNull TypeSerializerCollection getRegisteredTypeSerializers() {
        return serializers;
    }

    @Override
    public @NotNull Optional<? extends ConfigurationNode> getDataFor(@NotNull IRegion region) {
        return Optional.ofNullable(this.cachedData.get(region.uuid()));
    }

    @Override
    public @NotNull Optional<? extends ConfigurationNode> getDataFor(@NotNull UUID region) {
        return Optional.ofNullable(this.cachedData.get(region));
    }

    @Override
    public void removeData(@NotNull UUID region) {
        this.cachedData.remove(region);
    }

    @Override
    public void removeData(@NotNull IRegion region) {
        this.cachedData.remove(region.uuid());
    }

    @Override
    public void init() throws IOException {
        readDataFully();
    }

    protected abstract @NotNull Optional<@NotNull ConfigurationNode> readData(@NotNull UUID region);

    protected abstract @NotNull Set<@NotNull UUID> readKeys();

    protected abstract Map<@NotNull UUID, @NotNull ConfigurationNode> readDataFully();

}
