package com.gmail.andrewandy.regionshop.data;

import co.aikar.taskchain.TaskChainFactory;
import com.gmail.andrewandy.regionshop.configuration.InternalConfig;
import com.gmail.andrewandy.regionshop.region.IRegion;
import com.gmail.andrewandy.regionshop.util.LogUtils;
import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class JsonDataHandler extends AbstractRegionDataHandler {

    private static final String INDEX_NAME = "index.txt";
    private static final String DATA_DIR_NAME = "data";
    private static final GsonConfigurationLoader EMPTY_LOADER = GsonConfigurationLoader.builder().lenient(true).build();

    @Inject
    private LogUtils logUtils;
    @Inject
    private TaskChainFactory taskChainFactory;
    @Inject
    private InternalConfig config;
    @Inject
    @Named("internal-data-folder")
    private File internalDataFolder;

    private File root;

    @Override
    public void init() {
        this.root = new File(config.getDatabaseOptions().getDatabaseURL().replaceAll("%_", internalDataFolder.getAbsolutePath() + File.separator));
        this.root.mkdir();
        readDataFully();
    }

    @Override
    public @NotNull ConfigurationNode getOrCreateDataFor(@NotNull IRegion region) {
        final Optional<? extends ConfigurationNode> optional = getDataFor(region);
        if (optional.isPresent()) {
            return optional.get();
        }
        final ConfigurationNode node = EMPTY_LOADER.createNode();
        super.cachedData.put(region.getUUID(), node);
        return node;
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
            logUtils.logException(ex);
            return Optional.empty();
        }
    }

    @Override
    protected @NotNull Set<@NotNull UUID> readKeys() {
        final File index = new File(root, INDEX_NAME);
        final Set<UUID> set = new HashSet<>();
        final LogUtils.LogCollector collector = logUtils.newLogCollector();
        try (BufferedReader reader = Files.newBufferedReader(index.toPath());
             Scanner scanner = new Scanner(reader)) {
            scanner.useDelimiter(",");
            while (scanner.hasNext()) {
                final String next = scanner.next();
                try {
                    set.add(UUID.fromString(next));
                } catch (IllegalArgumentException ex) {
                    collector.log(Level.WARNING, "<yellow>Invalid uuid detected: " + next);
                }
            }
        } catch (IOException ex) {
            collector.logException(ex);
            collector.log(Level.SEVERE, "<red>Failed to read keys | JsonDataHandler</red>");
        }
        collector.dumpLog();
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
            logUtils.logException(ex);
            logUtils.log(Level.SEVERE, "<red>Failed to read keys | JsonDataHandler </red>");
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
        final LogUtils.LogCollector collector = logUtils.newLogCollector();
        for (String rawUUID : keys) {
            final UUID uuid;
            try {
                uuid = UUID.fromString(rawUUID);
            } catch (IllegalArgumentException iae) {
                collector.log(Level.WARNING, "<red>Skipping invalid uuid: " + rawUUID);
                continue;
            }
            try {
                final GsonConfigurationLoader loader = builder.file(new File(dataDir, rawUUID + ".json")).build();
                dataCache.put(uuid, loader.load());
            } catch (ConfigurateException ex) {
                collector.logException(ex);
                collector.log(Level.SEVERE, "<red>Failed to load data for uuid: " + rawUUID + "!</red>");
            }
        }
        super.cachedData.clear();
        super.cachedData.putAll(dataCache);
        collector.dumpLog();
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
        System.out.println(index.getAbsolutePath());
        // Rebuild index file.
        index.delete();
        final File data = new File(root, DATA_DIR_NAME);
        final LogUtils.LogCollector collector = logUtils.newLogCollector();
        for (Map.Entry<UUID, ConfigurationNode> entry : map.entrySet()) {
            final File target = new File(data, entry.getKey().toString() + ".json");
            try {
                final GsonConfigurationLoader loader = GsonConfigurationLoader.builder().file(target).build();
                loader.save(entry.getValue());
            } catch (IOException ex) {
                collector.logException(Level.WARNING, ex);
                collector.log(Level.WARNING, "Failed to update data file for uuid: " + entry.getKey().toString());
            }
        }
        collector.log(Level.INFO, "<white>Updating index file...</white>");
        try (BufferedWriter writer = Files.newBufferedWriter(index.toPath(), Charsets.UTF_8)) {
            for (UUID uuid : map.keySet()) {
                writer.write(uuid.toString() + ",");
                writer.newLine();
            }
            writer.flush();
        }
        collector.log(Level.INFO, "<white>Index file updated successfully!");
        collector.dumpLog();
    }

    @Override
    public @NotNull CompletableFuture<Void> flushChangesAsync() {
        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        taskChainFactory.newChain().async(() -> {
            try {
                flushChanges();
                completableFuture.complete(null);
            } catch (IOException ex) {
                if (!completableFuture.isDone()) {
                    completableFuture.completeExceptionally(ex);
                }
            }
        }).execute();
        return completableFuture;
    }
}
