package com.rouesvm.servback.registry;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class BackpackPersistentDataAttachmentRegistry {
    public static final AttachmentType<Boolean> PLACE_WITH_HAND_FULL = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "place_with_hand_full"),
            builder -> builder
                    .initializer(() -> false)
                    .persistent(Codec.BOOL.stable())
                    .copyOnDeath());
    public static final AttachmentType<Boolean> PLACE_WITH_OFFHAND_FULL = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "place_with_offhand_full"),
            builder -> builder
                    .initializer(() -> false)
                    .persistent(Codec.BOOL.stable())
                    .copyOnDeath());
    public static final AttachmentType<Boolean> EQUIP_WITH_HAND_FULL = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "equip_with_hand_full"),
            builder -> builder
                    .initializer(() -> false)
                    .persistent(Codec.BOOL.stable())
                    .copyOnDeath());
    public static final AttachmentType<Boolean> EQUIP_WITH_OFFHAND_FULL = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "equip_with_offhand_full"),
            builder -> builder
                    .initializer(() -> false)
                    .persistent(Codec.BOOL.stable())
                    .copyOnDeath());

    public static final AttachmentType<Boolean> BACKPACK_SELF_VISIBILITY = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "backpack_self_visiblity"),
            builder -> builder
                    .initializer(() -> true)
                    .persistent(Codec.BOOL.stable())
                    .copyOnDeath());
    public static final AttachmentType<Boolean> HIDE_BACKPACK_WHEN_LOOKING_DOWN = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(MOD_ID, "hide_backpack_when_looking_down"),
            builder -> builder
                    .initializer(() -> true)
                    .persistent(Codec.BOOL.stable())
                    .copyOnDeath());

    public static void initialize() {}
}
