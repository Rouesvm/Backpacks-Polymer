package com.rouesvm.servback.registry;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static com.rouesvm.servback.Main.MOD_ID;

public class DataComponentRegistry {
    public static final ComponentType<Boolean> BOOLEAN_TYPE = register(
            ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN).build(),
            "boolean"
    );

    public static final ComponentType<String> UUID_TYPE = register(
            ComponentType.<String>builder().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build(),
            "uuid"
    );

    private static <T> ComponentType<T> register(ComponentType<T> type, String name) {
        var registry = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, name), type);
        PolymerComponent.registerDataComponent(registry);
        return registry;
    }

    public static void initialize() {}
}
