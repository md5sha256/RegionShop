package com.gmail.andrewandy.regionshop.data;

import com.gmail.andrewandy.regionshop.RegionShop;
import com.gmail.andrewandy.regionshop.region.IRegion;
import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
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

final class SqliteDataHandler extends AbstractRegionDataHandler {

    private static final String PRIMARY_KEY = "uuid";
    private static final String DATA_KEY = "data";
    private static final String TABLE_NAME = "regionshop_regions";
    private final Set<UUID> removed = ConcurrentHashMap.newKeySet();
    @Inject
    private HikariDataSource dataSource;
    @Inject
    private RegionShop plugin;

    public SqliteDataHandler() {

    }

    @Override
    public void removeData(@NotNull UUID region) {
        super.removeData(region);
        removed.add(region);
    }

    @Override
    public void removeData(@NotNull IRegion region) {
        super.removeData(region);
        removed.add(region.getUUID());
    }

    @Override
    public final void init() throws IOException {
        try (Connection connection = dataSource.getConnection()) {
            statementInitTable(connection).close();
            readDataFully(connection);
        } catch (SQLException ex) {
            throw new IOException(ex);
        }
    }

    private PreparedStatement statementWriteData(@NotNull Connection connection, @NotNull UUID target, byte[] data) throws SQLException {
        final String sql = "INSERT INTO " + TABLE_NAME + "(" + PRIMARY_KEY + ", " + DATA_KEY + ") VALUES(?,?) ON DUPLICATE KEY UPDATE " + PRIMARY_KEY + "=?, " + DATA_KEY + "=?";
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        final SerialBlob blob = new SerialBlob(data);
        preparedStatement.setString(1, target.toString());
        preparedStatement.setBlob(2, blob);
        preparedStatement.setString(3, target.toString());
        preparedStatement.setBlob(4, blob);
        return preparedStatement;
    }

    private PreparedStatement statementUpdateDataBatch(@NotNull Connection connection, @NotNull Map<UUID, byte[]> targets) throws SQLException {
        final String sql = "INSERT INTO " + TABLE_NAME + "(" + PRIMARY_KEY + ", " + DATA_KEY + ") VALUES(?,?) ON DUPLICATE KEY UPDATE " + PRIMARY_KEY + "=?, " + DATA_KEY + "=?";
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

    @Override
    protected @NotNull Optional<@NotNull ConfigurationNode> readData(@NotNull UUID region) {
        return Optional.empty();
    }

    @Override
    protected @NotNull Set<@NotNull UUID> readKeys() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = statementReadKeysOnly(connection);
             ResultSet resultSet = statement.executeQuery()) {
            final Set<UUID> keys = new HashSet<>();
            while (resultSet.next()) {
                try {
                    keys.add(UUID.fromString(resultSet.getString(PRIMARY_KEY)));
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                    // FIXME LOG ERROR
                }
            }
            return keys;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Collections.emptySet();
    }

    @Override
    protected Map<@NotNull UUID, @NotNull ConfigurationNode> readDataFully() {
        try (Connection connection = dataSource.getConnection()) {
            return readDataFully(connection);
        } catch (SQLException ex) {
            // FIXME log error
            ex.printStackTrace();
        }
        return Collections.emptyMap();
    }

    private Map<@NotNull UUID, @NotNull ConfigurationNode> readDataFully(@NotNull Connection connection) {
        final Map<UUID, ConfigurationNode> map = new HashMap<>();
        try (PreparedStatement preparedStatement = statementReadData(connection);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                final UUID uuid;
                try {
                    uuid = UUID.fromString(resultSet.getString(PRIMARY_KEY));
                } catch (IllegalArgumentException ex) {
                    //FIXME LOG ERROR
                    continue;
                }
                final byte[] rawData = resultSet.getBytes(DATA_KEY);
                final String decoded = new String(rawData, Charsets.UTF_8);
                final GsonConfigurationLoader loader = GsonConfigurationLoader.builder().lenient(true).source(() -> new BufferedReader(new StringReader(decoded))).build();
                try {
                    map.put(uuid, loader.load());
                } catch (ConfigurateException ex) {
                    // FIXME log error
                }
            }
        } catch (SQLException ex) {
            super.cachedData.clear();
            // FIXME log error properly
            ex.printStackTrace();
            return Collections.emptyMap();
        }
        super.cachedData.clear();
        super.cachedData.putAll(map);
        return map;
    }

    @Override
    public void flushChanges() throws IOException {
        final Set<UUID> removedCopy = new HashSet<>(this.removed);
        final Map<UUID, byte[]> dataCopy = new HashMap<>();
        for (Map.Entry<UUID, ConfigurationNode> entry : this.cachedData.entrySet()) {
            try (final StringWriter writer = new StringWriter()) {
                final GsonConfigurationLoader loader = GsonConfigurationLoader.builder().sink(() -> new BufferedWriter(writer)).build();
                loader.save(entry.getValue());
                final byte[] data = writer.getBuffer().toString().getBytes(StandardCharsets.UTF_8);
                dataCopy.put(entry.getKey(), data);
            } catch (ConfigurateException ex) {
                // FIXME LOG ERROR
                ex.printStackTrace();
            }
        }
        this.removed.clear();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement deletionBatch = statementDeleteDataBatch(connection, removedCopy);
             PreparedStatement updateBatch = statementUpdateDataBatch(connection, dataCopy)) {
            deletionBatch.executeBatch();
            updateBatch.executeBatch();
        } catch (SQLException ex) {
            throw new IOException(ex);
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
