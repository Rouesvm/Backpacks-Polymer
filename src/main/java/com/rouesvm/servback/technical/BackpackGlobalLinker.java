package com.rouesvm.servback.technical;

import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class BackpackGlobalLinker {
    public static void testLink(ItemEntity source, World world) {
        if (world.isClient()) return;
        if (source.age < 60) return;
        if (source.isRemoved()) return;

        ItemStack stack = source.getStack();

        boolean isBackpack = stack.isOf(BackpackItemRegistry.GLOBAL_BACKPACK);

        if (!isBackpack) return;

        Box area = source.getBoundingBox().expand(2);

        List<ItemEntity> backpackEntities = world.getEntitiesByClass(ItemEntity.class, area, (itemEntity ->
                itemEntity != source &&
                        itemEntity.age > 10 &&
                        itemEntity.getStack().isOf(BackpackItemRegistry.GLOBAL_BACKPACK)));

        List<ItemEntity> enderPearlsEntities = world.getEntitiesByClass(ItemEntity.class, area, (itemEntity ->
                itemEntity != source &&
                        itemEntity.age > 10 &&
                        itemEntity.getStack().isOf(Items.ENDER_PEARL)));

        backpackEntities.add(source);
        ItemEntity enderPearl = enderPearlsEntities.isEmpty() ? null : enderPearlsEntities.getFirst();

        int globalBackpacks = backpackEntities.size();
        if (globalBackpacks < 2) return;

        if (enderPearl != null && source.isOnGround()) {
            link(backpackEntities, enderPearl);
        } else if (source.isSubmergedInWater()) unlink(backpackEntities);
    }

    public static void unlink(List<ItemEntity> entities) {
        if (entities.size() < 2) return;

        UUID firstUUID = BackpackUUID.getStackUUID(entities.getFirst().getStack());
        UUID secondUUID = BackpackUUID.getStackUUID(entities.get(1).getStack());

        if (firstUUID == null) return;
        if (secondUUID == null) return;

        if (!firstUUID.equals(secondUUID)) return;

        ItemStack backpackToUnlink = null;
        ItemStack sourceBackpack = null;

        ItemEntity backpackEntity = null;

        for (ItemEntity entity : entities) {
            ItemStack stack = entity.getStack();
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

            World world = backpackEntity.getWorld();
            Vec3d pos = backpackEntity.getPos();

            applyEffects((ServerWorld) world, pos);
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

        UUID firstUUID = BackpackUUID.getStackUUID(entities.get(0).getStack());
        UUID secondUUID = BackpackUUID.getStackUUID(entities.get(1).getStack());

        if (firstUUID != null && firstUUID.equals(secondUUID)) return;

        ItemStack sourceBackpack = null;
        ItemStack targetBackpack = null;

        for (ItemEntity entity : entities) {
            ItemStack stack = entity.getStack();
            DefaultedList<ItemStack> inventory = BackpackUtils.getItemList(stack);

            if (inventory.isEmpty()) {
                if (targetBackpack == null) targetBackpack = stack;
            } else if (sourceBackpack == null) sourceBackpack = stack;
        }

        if (performLink(entities, sourceBackpack, targetBackpack)) {
            World world = catalyst.getWorld();
            Vec3d pos = catalyst.getPos();

            catalyst.discard();
            applyEffects((ServerWorld) world, pos);
        }
    }

    private static boolean performLink(List<ItemEntity> entities, ItemStack sourceBackpack, ItemStack targetBackpack) {
        if (sourceBackpack == null) {
            sourceBackpack = entities.getFirst().getStack();
        }

        if (targetBackpack == null) {
            targetBackpack = entities.get(1).getStack();
        }

        int sourceCount = sourceBackpack.getOrDefault(BackpackDataComponentTypes.LINK_COUNT, 0);
        if (sourceCount >= 1) return false;


        UUID targetUUID = BackpackUUID.getStackUUID(targetBackpack);
        UUID sourceUUID = BackpackUUID.getStackUUID(sourceBackpack);

        if (targetUUID != null) {
            BackpackInventory inventory = BackpackManager.getInventory(targetUUID);
            if (inventory != null && !inventory.isEmpty()) {
                BackpackUtils.dropItems(entities.getFirst(), inventory, 0);
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

    private static void applyEffects(ServerWorld world, Vec3d pos) {
        world.spawnParticles(ParticleTypes.PORTAL, pos.x, pos.y + 0.5, pos.z,
                100, 0.5, 0.5, 0.5, 0.5);

        world.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.BLOCKS, 1.0f, 1.2f);
    }

    private static void applyVisuals(@Nullable ItemStack toApply) {
        if (toApply == null) return;
        toApply.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private static void removeVisuals(@Nullable ItemStack toApply) {
        if (toApply == null) return;
        toApply.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
    }
}
