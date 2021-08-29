package com.gmail.andrewandy.regionshop.region.settings;

import io.leangen.geantyref.GenericTypeReflector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Optional;

public class BaseSettingAccessor implements SettingAccessor {

    private final ConfigurationNode root;

    public BaseSettingAccessor(@NotNull ConfigurationNode node) {
        this.root = node;
    }

    @Override
    public @NotNull <T> Optional<T> querySetting( @NotNull ISetting<T> setting) {
        try {
            return Optional.ofNullable(this.root.node(setting.path()).get(setting.type()));
        } catch (SerializationException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public <T> void writeSetting(@NotNull ISetting<T> setting, @Nullable T value)
            throws SerializationException{
        this.root.node(setting.path()).set(setting.type(), value);
    }

}
