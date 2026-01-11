package com.rouesvm.servback.registry;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.content.component.LinkScrollerComponent;
import com.rouesvm.servback.content.component.UpgradeComponent;
import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

import java.util.UUID;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class BackpackDataComponentTypes {
    public static final DataComponentType<LinkScrollerComponent> LINK_SCROLLER = register(
            DataComponentType.<LinkScrollerComponent>builder().persistent(LinkScrollerComponent.CODEC).networkSynchronized(LinkScrollerComponent.PACKET_CODEC).build(),
            "link_scroller"
    );

    public static final DataComponentType<Integer> LINK_COUNT = register(
            DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build(),
            "link_count"
    );

    public static final DataComponentType<UpgradeContainerComponent> UPGRADE_CONTAINER = register(
            DataComponentType.<UpgradeContainerComponent>builder().persistent(UpgradeContainerComponent.CODEC).networkSynchronized(UpgradeContainerComponent.PACKET_CODEC).build(),
            "upgrade_container"
    );

    public static final DataComponentType<UpgradeComponent> UPGRADE = register(
            DataComponentType.<UpgradeComponent>builder().persistent(UpgradeComponent.CODEC).networkSynchronized(UpgradeComponent.PACKET_CODEC).build(),
            "upgrade"
    );

    public static final DataComponentType<UUID> BACKPACK_UUID = register(
            DataComponentType.<UUID>builder().persistent(UUIDUtil.AUTHLIB_CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build(),
            "backpack_uuid"
    );

    public static final DataComponentType<String> STRING_UUID = register(
            DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build(),
            "uuid"
    );

    public static final DataComponentType<Boolean> IS_OPENED = register(
            DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build(),
            "boolean"
    );

    private static <T> DataComponentType<T> register(DataComponentType<T> type, String name) {
        var registry = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name), type);
        PolymerComponent.registerDataComponent(registry);
        return registry;
    }

    public static void initialize() {}
}
