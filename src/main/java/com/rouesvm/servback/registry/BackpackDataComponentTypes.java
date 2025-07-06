package com.rouesvm.servback.registry;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class BackpackDataComponentTypes {
    public static final ComponentType<Boolean> BOOLEAN_TYPE = register(
            ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOLEAN).build(),
            "boolean"
    );

    public static final ComponentType<UUID> BACKPACK_UUID_TYPE = register(
            ComponentType.<UUID>builder().codec(Uuids.CODEC).packetCodec(Uuids.PACKET_CODEC).build(),
            "backpack_uuid"
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

    @SuppressWarnings("EmptyMethod")
    public static void initialize() {}
}
