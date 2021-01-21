package com.gmail.andrewandy.regionshop.configuration;

import com.gmail.andrewandy.regionshop.data.DatabaseType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Objects;

@ConfigSerializable
public class DatabaseOptions {

    @Setting(value = "type")
    @Comment(value = "Database type the plugin will use, available options are `json` and `sqlite`.")
    @Required
    private DatabaseType databaseType = DatabaseType.JSON;
    @Setting(value = "url")
    @Comment(value = "Where the database is located. Use %_ to denote the plugin base directory.")
    @Required
    private String databaseURL = "%_data";
    @Setting(value = "username")
    @Comment(value = "Username to connect to the database, not required for type=json")
    private String username = null;
    @Setting(value = "password")
    @Comment(value = "Password to connect to the database, not required for type=json")
    private String password = null;

    public static @NotNull DatabaseOptions defaultOptions() {
        final DatabaseOptions options = new DatabaseOptions();
        options.databaseType = DatabaseType.JSON;
        options.username = "null";
        options.password = "null";
        return options;
    }

    public DatabaseOptions() {}

    public DatabaseOptions(@NotNull DatabaseOptions other) {
        this.databaseType = other.getDatabaseType();
        this.username = other.username;
        this.password = other.password;
        this.databaseURL = other.databaseURL;
    }

    public @NotNull DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(@NotNull DatabaseType databaseType) {
        this.databaseType = Objects.requireNonNull(databaseType);
    }

    public @NotNull String getPassword() {
        return password;
    }

    public void setUsername(@Nullable String username) {
        this.username = username == null ? "null" : username;
    }

    public @NotNull String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password == null ? "null" : password;
    }

    public @NotNull String getDatabaseURL() {
        return databaseURL;
    }

    public void setDatabaseURL(@NotNull String databaseURL) {
        this.databaseURL = Objects.requireNonNull(databaseURL);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatabaseOptions that = (DatabaseOptions) o;
        return databaseType == that.databaseType && databaseURL.equals(that.databaseURL) && username.equals(that.username) && password.equals(that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(databaseType, databaseURL, username, password);
    }
}
