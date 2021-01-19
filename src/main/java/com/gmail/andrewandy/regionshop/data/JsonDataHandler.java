package com.gmail.andrewandy.regionshop.data;

import com.gmail.andrewandy.regionshop.RegionShop;
import com.gmail.andrewandy.regionshop.region.IRegion;
import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;

final class JsonDataHandler extends AbstractRegionDataHandler {

    private static final String INDEX_NAME = "index.txt";
    private static final String DATA_DIR_NAME = "data";
    private final File root;
    @Inject
    private RegionShop plugin;

    @AssistedInject
    public JsonDataHandler(@Assisted @NotNull File path) {
        this.root = path;
        readDataFully();
    }

    @Override
    protected @NotNull Optional<@NotNull ConfigurationNode> readData(@NotNull UUID region) {
        final File file = new File(DATA_DIR_NAME, region.toString() + ".json");
        if (!file.isFile()) {
            return Optional.empty();
        }
        try {
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().file(file).lenient(true).build();
            final ConfigurationNode node = loader.load();
            super.cachedData.put(region, node);
            return Optional.of(node);
        } catch (ConfigurateException ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    protected @NotNull Set<@NotNull UUID> readKeys() {
        final File index = new File(root, INDEX_NAME);
        final Set<UUID> set = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(index.toPath());
             Scanner scanner = new Scanner(reader)) {
            scanner.useDelimiter(",");
            while (scanner.hasNext()) {
                try {
                    set.add(UUID.fromString(scanner.next()));
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            // FIXME log error
        }
        return set;
    }

    protected final @NotNull Set<@NotNull String> readKeysRaw() {
        final File index = new File(root, INDEX_NAME);
        final Set<String> set = new HashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(index.toPath());
             Scanner scanner = new Scanner(reader)) {
            scanner.useDelimiter(",");
            while (scanner.hasNext()) {
                set.add(scanner.next());
            }
        } catch (IOException ex) {
            // FIXME log error
        }
        return set;
    }

    /**
     * Thread-safe method to read and cached all data into the data handler.
     *
     * @return Returns a copy of the cached data, with the Keys representing the {@link IRegion#getUUID()} and values representing
     * the data container.
     */
    @Override
    public synchronized Map<@NotNull UUID, @NotNull ConfigurationNode> readDataFully() {
        final File index = new File(root, INDEX_NAME);
        final File dataDir = new File(root, DATA_DIR_NAME);
        if (!dataDir.exists()) {
            index.delete();
            dataDir.mkdir();
            return Collections.emptyMap();
        }
        final Set<String> keys = readKeysRaw();
        // TODO validate
        final Map<UUID, ConfigurationNode> dataCache = new HashMap<>(keys.size());
        final GsonConfigurationLoader.Builder builder = GsonConfigurationLoader.builder().lenient(true);
        for (String rawUUID : keys) {
            final UUID uuid;
            try {
                uuid = UUID.fromString(rawUUID);
            } catch (IllegalArgumentException iae) {
                // FIXME log error
                continue;
            }
            try {
                final GsonConfigurationLoader loader = builder.file(new File(dataDir, rawUUID + ".json")).build();
                dataCache.put(uuid, loader.load());
            } catch (ConfigurateException ex) {
                // FIXME queue error.
            }
        }
        super.cachedData.clear();
        super.cachedData.putAll(dataCache);
        return dataCache;
    }

    @Override
    public synchronized void flushChanges() throws IOException {
        final Map<UUID, ConfigurationNode> map = new HashMap<>(super.cachedData.size());
        // Deep copy of the map before flushing changes
        for (Map.Entry<UUID, ConfigurationNode> entry : super.cachedData.entrySet()) {
            map.put(entry.getKey(), entry.getValue().copy());
        }
        final File index = new File(root, INDEX_NAME);
        // Rebuild index file.
        index.delete();
        final File data = new File(root, DATA_DIR_NAME);
        for (Map.Entry<UUID, ConfigurationNode> entry : map.entrySet()) {
            final File target = new File(data, entry.getKey().toString() + ".json");
            try {
                final GsonConfigurationLoader loader = GsonConfigurationLoader.builder().file(target).build();
                loader.save(entry.getValue());
            } catch (IOException ex) {
                // FIXME log error properly

                // Remove the data file if we fail to write the data
                target.delete();

            }
        }
        try (BufferedWriter writer = Files.newBufferedWriter(index.toPath(), Charsets.UTF_8)) {
            for (UUID uuid : map.keySet()) {
                writer.write(uuid.toString() + ",");
                writer.newLine();
            }
            writer.flush();
        }

    }

    @Override
    public @NotNull CompletableFuture<Void> flushChangesAsync() {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                flushChanges();
                completableFuture.complete(null);
            } catch (IOException ex) {
                if (!completableFuture.isDone()) {
                    completableFuture.completeExceptionally(ex);
                }
            }
        });
        return completableFuture;
    }
}
