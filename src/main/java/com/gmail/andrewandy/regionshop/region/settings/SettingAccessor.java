package com.gmail.andrewandy.regionshop.region.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Optional;

public interface SettingAccessor {

    <T> @NotNull Optional<T> querySetting(@NotNull ISetting<T> setting);

    <T> void writeSetting(@NotNull ISetting<T> setting,@Nullable T value)
            throws SerializationException;

}
