package com.github.md5sha256.regionshop.region.feature.builtins.access;

import com.github.md5sha256.regionshop.data.RegionDataHandler;
import io.leangen.geantyref.TypeToken;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.*;

import javax.inject.Inject;
import java.lang.reflect.Type;

public class AccessDataSerializer implements TypeSerializer<AccessData> {

    private static final String CLASS_KEY = "class";
    private static final String DATA_KEY = "data";

    @Inject
    private RegionDataHandler dataHandler;

    public AccessDataSerializer() {
    }

    @Override
    public AccessData deserialize(Type type, ConfigurationNode node) throws SerializationException {
        final String rawClass = node.node(CLASS_KEY).getString(null);
        final TypeSerializerCollection serializers = dataHandler.getRegisteredTypeSerializers();
        if (rawClass != null) {
            // If a class was found, try to validate
            try {
                final Class<?> clazz = Class.forName(rawClass);
                if (!AccessData.class.isAssignableFrom(clazz)) {
                    // Validation failed
                    throw new SerializationException(rawClass + " does not implement AccessData!");
                }
                if (!clazz.isEnum()) {
                    // If class is not an enum, try to find a registered deserializer
                    // from shared TypeSerializerConnection; RegionDataHandler#getRegisteredTypeSerializers
                    final TypeSerializer<? extends AccessData> serializer = serializers
                            .get(TypeToken.get(clazz.asSubclass(AccessData.class)));
                    if (serializer == null) {
                        // No serializer found
                        throw new SerializationException("No serializer registered for class: " + rawClass);
                    }
                    // Use the alternate deserializer
                    return serializer.deserialize(type, node.node(DATA_KEY));
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        // Try enum deserialization blind
        ScalarSerializer<Enum<?>> enumSerializer = Scalars.ENUM;
        final Enum<?> value = enumSerializer.deserialize(type, node);
        if (!value.getDeclaringClass().isAssignableFrom(AccessData.class)) {
            // Invalid class
            throw new SerializationException(value.getDeclaringClass().getCanonicalName() + " does not implement AccessData!");
        }
        return (AccessData) value;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void serialize(Type type, @Nullable AccessData obj, ConfigurationNode node) throws SerializationException {
        if (obj == null) {
            node.raw(null);
            return;
        }
        if (obj instanceof Enum) {
            final String rawClass = obj.getClass().getCanonicalName();
            node.node(CLASS_KEY).set(rawClass);
            node.node(DATA_KEY).set(Enum.class, (Enum<?>) obj);
            return;
        }
        final TypeSerializerCollection serializers = dataHandler.getRegisteredTypeSerializers();
        final TypeSerializer serializer = serializers
                .get(TypeToken.get(obj.getClass()));
        if (serializer == null) {
            // No serializer found
            throw new SerializationException("No serializer registered for class: " + obj.getClass());
        }
        serializer.serialize(type, obj, node);
    }
}
