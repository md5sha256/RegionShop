package com.github.md5sha256.regionshop.configuration;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

public class ConfigurationTransformer {

    public static final int LATEST_VERSION = 1;

    /**
     * Update + validate the {@link InternalConfig}'s raw data types before loading.
     * @param configurationNode The {@link ConfigurationNode} to validate, update and load.
     * @return Returns the {@link InternalConfig} object.
     * @throws IllegalStateException If serialization errors / data validation errors occur.
     */
    public @NotNull InternalConfig process(final ConfigurationNode configurationNode) {
        if (configurationNode.empty()) {
            try {
                final InternalConfig config = new InternalConfig();
                configurationNode.set(config);
                return config;
            } catch (SerializationException ex) {
                // Should never happen
                throw new IllegalStateException("Failed to save default options!", ex);
            }
        }
        final int version = configurationNode.node("config-version").getInt(-1);
        if (version == -1 || version > LATEST_VERSION) {
            throw new IllegalStateException("Invalid Configuration Version Detected: " + version);
        }
        if (version < LATEST_VERSION) {
            for (int current = 0; current < LATEST_VERSION; current++) {
                transform(configurationNode, current);
            }
        }
        try {
            final InternalConfig config = configurationNode.get(InternalConfig.class);
            assert config != null;
            return config;
        } catch (SerializationException ex) {
            throw new IllegalStateException("Invalid Configuration Detected!", ex);
        }
    }

    private void transform(ConfigurationNode configurationNode, int current) {

    }

}
