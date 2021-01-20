package com.gmail.andrewandy.regionshop.configuration;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Objects;

@ConfigSerializable
public class InternalConfig implements RegionShopConfig {

    @Comment("Config version, do not change!")
    @Setting("version")
    private int configVersion = 1;

    @Setting(value = "log-log-prefix")
    @Comment(value = "Prefix the plugin will use when logging to console. Default value: <gray>[<gold>RegionShop</gold>]</gray>")
    private String logPrefix = "<gray>[<gold>RegionShop</gold>]</gray>";

    @Setting(value = "database")
    @Comment(value = "Settings relating to how data is stored.")
    @Required
    private DatabaseOptions databaseOptions = DatabaseOptions.defaultOptions();

    public void setValues(@NotNull RegionShopConfig other) {
        if (other.getConfigVersion() != this.configVersion) {
            throw new IllegalArgumentException("Config version mismatch!");
        }
        this.logPrefix = other.getLogPrefix();
        this.databaseOptions = new DatabaseOptions(other.getDatabaseOptions());
    }

    @Override
    public int getConfigVersion() {
        return configVersion;
    }

    @Override
    public @NotNull String getLogPrefix() {
        return logPrefix;
    }

    @Override
    public void setLogPrefix(@NotNull String logPrefix) {
        this.logPrefix = Objects.requireNonNull(logPrefix);
    }

    @Override
    public void setDatabaseOption(@NotNull DatabaseOptions databaseOptions) {
        this.databaseOptions = Objects.requireNonNull(databaseOptions);
    }

    @Override
    public @NotNull DatabaseOptions getDatabaseOptions() {
        return databaseOptions;
    }


}
