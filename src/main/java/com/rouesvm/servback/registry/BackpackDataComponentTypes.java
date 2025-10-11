package com.rouesvm.servback.registry;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.content.component.LinkScrollerComponent;
import com.rouesvm.servback.content.component.UpgradeComponent;
import com.rouesvm.servback.content.component.UpgradeContainerComponent;
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
    public static final ComponentType<LinkScrollerComponent> LINK_SCROLLER = register(
            ComponentType.<LinkScrollerComponent>builder().codec(LinkScrollerComponent.CODEC).packetCodec(LinkScrollerComponent.PACKET_CODEC).build(),
            "link_scroller"
    );

    public static final ComponentType<Integer> LINK_COUNT = register(
            ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.INTEGER).build(),
            "link_count"
    );

    public static final ComponentType<UpgradeContainerComponent> UPGRADE_CONTAINER = register(
            ComponentType.<UpgradeContainerComponent>builder().codec(UpgradeContainerComponent.CODEC).packetCodec(UpgradeContainerComponent.PACKET_CODEC).build(),
            "upgrade_container"
    );

    public static final ComponentType<UpgradeComponent> UPGRADE = register(
            ComponentType.<UpgradeComponent>builder().codec(UpgradeComponent.CODEC).packetCodec(UpgradeComponent.PACKET_CODEC).build(),
            "upgrade"
    );

    public static final ComponentType<UUID> BACKPACK_UUID = register(
            ComponentType.<UUID>builder().codec(Uuids.CODEC).packetCodec(Uuids.PACKET_CODEC).build(),
            "backpack_uuid"
    );

    public static final ComponentType<String> STRING_UUID = register(
            ComponentType.<String>builder().codec(Codec.STRING).packetCodec(PacketCodecs.STRING).build(),
            "uuid"
    );

    public static final ComponentType<Boolean> IS_3D = register(
            ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build(),
            "boolean"
    );

    private static <T> ComponentType<T> register(ComponentType<T> type, String name) {
        var registry = Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, name), type);
        PolymerComponent.registerDataComponent(registry);
        return registry;
    }

    public static void initialize() {}
}
