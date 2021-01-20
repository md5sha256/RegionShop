package com.gmail.andrewandy.regionshop.data;

import com.gmail.andrewandy.regionshop.region.IRegion;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RegionDataHandler {

    void init() throws IOException;

    @NotNull Optional<? extends ConfigurationNode> getDataFor(@NotNull IRegion region);

    @NotNull Optional<? extends ConfigurationNode> getDataFor(@NotNull UUID region);

    @NotNull ConfigurationNode getOrCreateDataFor(@NotNull IRegion region);

    void removeData(@NotNull UUID region);

    void removeData(@NotNull IRegion region);

    void flushChanges() throws IOException;

    @NotNull CompletableFuture<Void> flushChangesAsync();

}
