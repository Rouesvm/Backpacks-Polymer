package com.rouesvm.servback.technical;

import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class BackpackGlobalLinker {
    public static void testLink(ItemEntity source, Level world) {
        if (world.isClientSide()) return;
        if (source.tickCount < 60) return;
        if (source.isRemoved()) return;

        ItemStack stack = source.getItem();

        boolean isBackpack = stack.is(BackpackItemRegistry.GLOBAL_BACKPACK);

        if (!isBackpack) return;

        AABB area = source.getBoundingBox().inflate(2);

        List<ItemEntity> backpackEntities = world.getEntitiesOfClass(ItemEntity.class, area, (itemEntity ->
                itemEntity != source &&
                        itemEntity.tickCount > 10 &&
                        itemEntity.getItem().is(BackpackItemRegistry.GLOBAL_BACKPACK)));

        List<ItemEntity> enderPearlsEntities = world.getEntitiesOfClass(ItemEntity.class, area, (itemEntity ->
                itemEntity != source &&
                        itemEntity.tickCount > 10 &&
                        itemEntity.getItem().is(Items.ENDER_PEARL)));

        backpackEntities.add(source);
        ItemEntity enderPearl = enderPearlsEntities.isEmpty() ? null : enderPearlsEntities.getFirst();

        int globalBackpacks = backpackEntities.size();
        if (globalBackpacks < 2) return;

        if (enderPearl != null && source.onGround()) {
            link(backpackEntities, enderPearl);
        } else if (source.isUnderWater()) unlink(backpackEntities);
    }

    public static void unlink(List<ItemEntity> entities) {
        if (entities.size() < 2) return;

        UUID firstUUID = BackpackUUID.getStackUUID(entities.getFirst().getItem());
        UUID secondUUID = BackpackUUID.getStackUUID(entities.get(1).getItem());

        if (firstUUID == null) return;
        if (secondUUID == null) return;

        if (!firstUUID.equals(secondUUID)) return;

        ItemStack backpackToUnlink = null;
        ItemStack sourceBackpack = null;

        ItemEntity backpackEntity = null;

        for (ItemEntity entity : entities) {
            ItemStack stack = entity.getItem();
            UpgradeContainerComponent containerComponent = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);

            if (containerComponent == null || containerComponent.baseUpgrades().isEmpty()) {
                if (backpackToUnlink == null) {
                    backpackToUnlink = stack;
                    backpackEntity = entity;
                    continue;
                }
            }

            sourceBackpack = stack;
        }

        removeVisuals(backpackToUnlink);
        removeVisuals(sourceBackpack);

        if (backpackToUnlink != null) {
            BackpackUUID.createNewUUID(backpackToUnlink);

            Level world = backpackEntity.level();
            Vec3 pos = backpackEntity.position();

            applyEffects((ServerLevel) world, pos);
        }

        if (sourceBackpack != null) {
            int sourceCount = sourceBackpack.getOrDefault(BackpackDataComponentTypes.LINK_COUNT, 0);
            sourceBackpack.set(BackpackDataComponentTypes.LINK_COUNT, sourceCount - 1);

            if (backpackToUnlink != null) {
                backpackToUnlink.set(BackpackDataComponentTypes.LINK_COUNT, sourceBackpack.get(BackpackDataComponentTypes.LINK_COUNT));
            }
        }
    }

    public static void link(List<ItemEntity> entities, ItemEntity catalyst) {
        if (entities.size() < 2) return;

        UUID firstUUID = BackpackUUID.getStackUUID(entities.get(0).getItem());
        UUID secondUUID = BackpackUUID.getStackUUID(entities.get(1).getItem());

        if (firstUUID != null && firstUUID.equals(secondUUID)) return;

        ItemStack sourceBackpack = null;
        ItemStack targetBackpack = null;

        for (ItemEntity entity : entities) {
            ItemStack stack = entity.getItem();
            NonNullList<ItemStack> inventory = BackpackUtils.getItemList(stack);

            if (inventory.isEmpty()) {
                if (targetBackpack == null) targetBackpack = stack;
            } else if (sourceBackpack == null) sourceBackpack = stack;
        }

        if (performLink(entities, sourceBackpack, targetBackpack)) {
            Level world = catalyst.level();
            Vec3 pos = catalyst.position();

            catalyst.discard();
            applyEffects((ServerLevel) world, pos);
        }
    }

    private static boolean performLink(List<ItemEntity> entities, ItemStack sourceBackpack, ItemStack targetBackpack) {
        if (sourceBackpack == null) {
            sourceBackpack = entities.getFirst().getItem();
        }

        if (targetBackpack == null) {
            targetBackpack = entities.get(1).getItem();
        }

        int sourceCount = sourceBackpack.getOrDefault(BackpackDataComponentTypes.LINK_COUNT, 0);
        if (sourceCount >= 1) return false;


        UUID targetUUID = BackpackUUID.getStackUUID(targetBackpack);
        UUID sourceUUID = BackpackUUID.getStackUUID(sourceBackpack);

        if (targetUUID != null) {
            BackpackInventory inventory = BackpackManager.getInventory(targetUUID);
            if (inventory != null && !inventory.isEmpty()) {
                BackpackUtils.dropItems(entities.getFirst().level(), entities.getFirst().blockPosition(), inventory, 0);
            }
        }

        if (sourceUUID == null) {
            sourceUUID = BackpackUUID.generateUniqueUUID();
            sourceBackpack.set(BackpackDataComponentTypes.BACKPACK_UUID, sourceUUID);
        }

        targetBackpack.set(BackpackDataComponentTypes.BACKPACK_UUID, sourceUUID);

        applyVisuals(targetBackpack);
        applyVisuals(sourceBackpack);

        sourceBackpack.set(BackpackDataComponentTypes.LINK_COUNT, sourceCount + 1);
        targetBackpack.set(BackpackDataComponentTypes.LINK_COUNT, sourceBackpack.get(BackpackDataComponentTypes.LINK_COUNT));

        return true;
    }

    private static void applyEffects(ServerLevel world, Vec3 pos) {
        world.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 0.5, pos.z,
                100, 0.5, 0.5, 0.5, 0.5);

        world.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.BLOCKS, 1.0f, 1.2f);
    }

    private static void applyVisuals(@Nullable ItemStack toApply) {
        if (toApply == null) return;
        toApply.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private static void removeVisuals(@Nullable ItemStack toApply) {
        if (toApply == null) return;
        toApply.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
    }
}
