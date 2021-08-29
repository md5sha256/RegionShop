package com.gmail.andrewandy.regionshop.data;

import co.aikar.taskchain.TaskChainFactory;
import com.gmail.andrewandy.regionshop.region.IRegion;
import com.gmail.andrewandy.regionshop.util.LogUtils;
import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.zaxxer.hikari.pool.HikariPool;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import javax.sql.rowset.serial.SerialBlob;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class SqliteDataHandler extends AbstractRegionDataHandler {

    private static final String PRIMARY_KEY = "uuid";
    private static final String DATA_KEY = "json_data";
    private static final String TABLE_NAME = "regionshop_regions";
    private static final GsonConfigurationLoader EMPTY_LOADER = GsonConfigurationLoader.builder().lenient(true).build();
    private final Set<UUID> removed = ConcurrentHashMap.newKeySet();
    @Inject
    private HikariPool dataSource;
    @Inject
    private TaskChainFactory taskChainFactory;
    @Inject
    private LogUtils logUtils;

    public SqliteDataHandler() {

    }

    @Override
    public final void init() throws IOException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement initTable = statementInitTable(connection)) {
            initTable.execute();
            readDataFully(connection);
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public @NotNull ConfigurationNode getOrCreateDataFor(@NotNull IRegion region) {
        final Optional<? extends ConfigurationNode> optional = getDataFor(region);
        if (optional.isPresent()) {
            return optional.get();
        }
        final ConfigurationNode node = EMPTY_LOADER.createNode();
        super.cachedData.put(region.uuid(), node);
        return node;
    }

    @Override
    public void removeData(@NotNull UUID region) {
        super.removeData(region);
        removed.add(region);
    }

    @Override
    public void removeData(@NotNull IRegion region) {
        super.removeData(region);
        removed.add(region.uuid());
    }

    @Override
    protected @NotNull Optional<@NotNull ConfigurationNode> readData(@NotNull UUID region) {
        return Optional.empty();
    }

    @Override
    protected @NotNull Set<@NotNull UUID> readKeys() {
        final LogUtils.LogCollector collector = logUtils.newLogCollector();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = statementReadKeysOnly(connection);
             ResultSet resultSet = statement.executeQuery()) {
            final Set<UUID> keys = new HashSet<>();
            while (resultSet.next()) {
                final String raw = resultSet.getString(PRIMARY_KEY);
                try {
                    keys.add(UUID.fromString(raw));
                } catch (IllegalArgumentException ex) {
                    collector.log(Level.WARNING, "<yellow>Invalid UUID detected: " + raw);
                }
            }
            return keys;

        } catch (SQLException ex) {
            collector.log(Level.SEVERE, "<red>Database error when reading keys | SqliteDataHandler</red>");
            collector.logException(ex);
        } finally {
            collector.dumpLog();
        }
        return Collections.emptySet();
    }

    @Override
    protected Map<@NotNull UUID, @NotNull ConfigurationNode> readDataFully() {
        final LogUtils.LogCollector collector = logUtils.newLogCollector();
        try (Connection connection = dataSource.getConnection()) {
            return readDataFully(connection);
        } catch (SQLException ex) {
            collector.logException(ex);
            collector.log(Level.SEVERE, "<red>Database error when reading keys | SqliteDataHandler</red>");
        } finally {
            collector.dumpLog();
        }
        return Collections.emptyMap();
    }

    private Map<@NotNull UUID, @NotNull ConfigurationNode> readDataFully(@NotNull Connection connection) {
        final Map<UUID, ConfigurationNode> map = new HashMap<>();
        final LogUtils.LogCollector collector = logUtils.newLogCollector();
        try (PreparedStatement preparedStatement = statementReadData(connection);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                final UUID uuid;
                final String raw = resultSet.getString(PRIMARY_KEY);
                try {
                    uuid = UUID.fromString(raw);
                } catch (IllegalArgumentException ex) {
                    collector.log(Level.WARNING, "<yellow>Invalid UUID detected: " + raw);
                    continue;
                }
                final byte[] rawData = resultSet.getBytes(DATA_KEY);
                final String decoded = new String(rawData, Charsets.UTF_8);
                final GsonConfigurationLoader loader = GsonConfigurationLoader.builder().lenient(true).source(() -> new BufferedReader(new StringReader(decoded))).build();
                try {
                    map.put(uuid, loader.load());
                } catch (ConfigurateException ex) {
                    collector.logException(ex);
                    collector.log(Level.SEVERE, "<red>Failed to load data for uuid: " + uuid);
                }
            }
        } catch (SQLException ex) {
            super.cachedData.clear();
            collector.logException(ex);
            collector.log(Level.SEVERE, "<red>Database error when loading data | SqliteDataHandler</red>");
            return Collections.emptyMap();
        } finally {
            collector.dumpLog();
        }
        super.cachedData.clear();
        super.cachedData.putAll(map);
        return map;
    }

    @Override
    public void flushChanges() throws IOException {
        final Set<UUID> removedCopy = new HashSet<>(this.removed);
        final Map<UUID, byte[]> dataCopy = new HashMap<>();
        final LogUtils.LogCollector collector = logUtils.newLogCollector();
        for (Map.Entry<UUID, ConfigurationNode> entry : this.cachedData.entrySet()) {
            try (final StringWriter writer = new StringWriter()) {
                final GsonConfigurationLoader loader = GsonConfigurationLoader.builder().sink(() -> new BufferedWriter(writer)).build();
                loader.save(entry.getValue());
                final byte[] data = writer.getBuffer().toString().getBytes(StandardCharsets.UTF_8);
                dataCopy.put(entry.getKey(), data);
            } catch (ConfigurateException ex) {
                collector.logException(ex);
                collector.log(Level.SEVERE, "<red>Failed to save data for uuid: " + entry.getKey().toString());
            }
        }
        // Clear the "removed" cache
        this.removed.clear();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement deletionBatch = statementDeleteDataBatch(connection, removedCopy);
             PreparedStatement updateBatch = statementUpdateDataBatch(connection, dataCopy)) {
            deletionBatch.executeBatch();
            updateBatch.executeBatch();
        } catch (SQLException ex) {
            throw new IOException(ex);
        } finally {
            collector.dumpLog();
        }
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

    private PreparedStatement statementWriteData(@NotNull Connection connection, @NotNull UUID target, byte[] data) throws SQLException {
        final String sql = "INSERT INTO " + TABLE_NAME + " (" + PRIMARY_KEY + ", " + DATA_KEY + ") VALUES(?,?) ON CONFLICT (" + PRIMARY_KEY + ") DO UPDATE SET " + PRIMARY_KEY + "=?, " + DATA_KEY + "=?";
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        final SerialBlob blob = new SerialBlob(data);
        preparedStatement.setString(1, target.toString());
        preparedStatement.setBlob(2, blob);
        preparedStatement.setString(3, target.toString());
        preparedStatement.setBlob(4, blob);
        return preparedStatement;
    }

    private PreparedStatement statementUpdateDataBatch(@NotNull Connection connection, @NotNull Map<UUID, byte[]> targets) throws SQLException {
        final String sql = "INSERT INTO " + TABLE_NAME + " (" + PRIMARY_KEY + ", " + DATA_KEY + ") VALUES(?,?) ON CONFLICT (" + PRIMARY_KEY + ") DO UPDATE SET " + PRIMARY_KEY + "=?, " + DATA_KEY + "=?";
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        for (Map.Entry<UUID, byte[]> entry : targets.entrySet()) {
            final UUID target = entry.getKey();
            final byte[] data = entry.getValue();
            final SerialBlob blob = new SerialBlob(data);
            preparedStatement.setString(1, target.toString());
            preparedStatement.setBlob(2, blob);
            preparedStatement.setString(3, target.toString());
            preparedStatement.setBlob(4, blob);
            preparedStatement.addBatch();
        }
        return preparedStatement;
    }

    private PreparedStatement statementInitTable(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + PRIMARY_KEY + " TEXT PRIMARY KEY, " + DATA_KEY + " BLOB)");
    }

    private PreparedStatement statementReadKeysOnly(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement("SELECT " + PRIMARY_KEY + " FROM " + TABLE_NAME);
    }

    private PreparedStatement statementReadData(@NotNull Connection connection) throws SQLException {
        return connection.prepareStatement("SELECT " + PRIMARY_KEY + ", " + DATA_KEY + " FROM " + TABLE_NAME);
    }

    private PreparedStatement statementReadData(@NotNull Connection connection, @NotNull UUID target) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement("SELECT " + PRIMARY_KEY + ", " + DATA_KEY + " FROM " + TABLE_NAME + " WHERE uuid=?");
        preparedStatement.setString(1, target.toString());
        return preparedStatement;
    }

    private PreparedStatement statementDeleteData(@NotNull Connection connection, @NotNull UUID target) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE " + PRIMARY_KEY + "=?");
        preparedStatement.setString(1, target.toString());
        return preparedStatement;
    }

    private PreparedStatement statementDeleteDataBatch(@NotNull Connection connection, @NotNull Collection<UUID> targets) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM " + TABLE_NAME + " WHERE " + PRIMARY_KEY + "=?");
        for (UUID uuid : targets) {
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.addBatch();
        }
        return preparedStatement;
    }

}
