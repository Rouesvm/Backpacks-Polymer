package com.rouesvm.servback.mixin;

import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.manager.BackpackUUID;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        ItemEntity source = (ItemEntity) (Object) this;
        World world = source.getEntityWorld();

        if (!world.isClient() && source.age == 60 &&
                (source.getStack().isOf(Items.ENDER_PEARL))) {

            Vec3d pos = source.getEntityPos();
            Box area = Box.of(pos, 4.0, 4.0, 4.0);

            List<ItemEntity> itemEntities = world.getEntitiesByClass(ItemEntity.class, area, (itemEntity -> itemEntity != source &&
                    itemEntity.age > 10 &&
                    itemEntity.getStack().isOf(BackpackItemRegistry.GLOBAL_BACKPACK)));

            int globalBackpacks = itemEntities.size();
            if (globalBackpacks >= 2) {
                link(itemEntities, source);
            }
        }
    }

    @Unique
    private static void link(List<ItemEntity> entities, ItemEntity catalyst) {
        if (entities.size() < 2) return;

        UUID firstUUID = BackpackUUID.getStackUUID(entities.get(0).getStack());
        UUID secondUUID = BackpackUUID.getStackUUID(entities.get(1).getStack());

        if (firstUUID != null
                && firstUUID.equals(secondUUID)
        ) return;

        ItemStack sourceBackpack = null;
        ItemStack targetBackpack = null;

        for (ItemEntity entity : entities) {
            ItemStack stack = entity.getStack();
            DefaultedList<ItemStack> inventory = BackpackUtils.getItemList(stack);

            if (inventory.isEmpty()) {
                if (targetBackpack == null) targetBackpack = entity.getStack();
            } else if (sourceBackpack == null) sourceBackpack = entity.getStack();
        }

        if (checkLink(entities, sourceBackpack, targetBackpack)) performLinkingResult(catalyst);
    }

    @Unique
    private static boolean checkLink(List<ItemEntity> entities, ItemStack sourceBackpack, ItemStack targetBackpack) {
        if (sourceBackpack == null) {
            sourceBackpack = entities.getFirst().getStack();
        }

        if (targetBackpack == null) {
            targetBackpack = entities.get(1).getStack();
        }

        int sourceCount = sourceBackpack.getOrDefault(BackpackDataComponentTypes.LINK_COUNT, 0);
        if (sourceCount >= 1) return false;

        UUID sourceUUID = BackpackUUID.getStackUUID(sourceBackpack);
        if (sourceUUID == null) {
            sourceUUID = UUID.randomUUID();
            sourceBackpack.set(BackpackDataComponentTypes.BACKPACK_UUID, sourceUUID);
        }

        targetBackpack.set(BackpackDataComponentTypes.BACKPACK_UUID, sourceUUID);

        targetBackpack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        sourceBackpack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);

        sourceBackpack.set(BackpackDataComponentTypes.LINK_COUNT, sourceCount + 1);
        targetBackpack.set(BackpackDataComponentTypes.LINK_COUNT, sourceBackpack.get(BackpackDataComponentTypes.LINK_COUNT));

        return true;
    }

    @Unique
    private static void performLinkingResult(ItemEntity catalyst) {
        World world = catalyst.getEntityWorld();
        Vec3d pos = catalyst.getEntityPos();

        catalyst.discard();

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.PORTAL, pos.x, pos.y + 0.5, pos.z,
                    100, 0.5, 0.5, 0.5, 0.5);
            world.playSound(null, pos.x, pos.y, pos.z,
                    SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                    SoundCategory.BLOCKS, 1.0f, 1.2f);
        }
    }
}
