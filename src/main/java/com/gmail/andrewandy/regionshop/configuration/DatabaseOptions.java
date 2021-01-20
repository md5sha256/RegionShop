package com.gmail.andrewandy.regionshop.configuration;

import com.gmail.andrewandy.regionshop.data.DatabaseType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

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

    public @NotNull DatabaseType getDatabaseType() {
        return databaseType;
    }

    public @NotNull String getPassword() {
        return password;
    }

    public @NotNull String getUsername() {
        return username;
    }

    public @NotNull String getDatabaseURL() {
        return databaseURL;
    }
}
