package com.rouesvm.servback.compat.trinkets;

import com.rouesvm.servback.content.block.BasicBackpackBlock;
import com.rouesvm.servback.content.block.BasicBackpackBlockEntity;
import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.BundleGuiItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackPersistentDataAttachmentRegistry;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.cosmetic.BackHolder;
import com.rouesvm.servback.technical.cosmetic.CosmeticManager;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.UUID;

public class BackpackTrinket implements TrinketCallback {
    public static void initialize() {
        UseBlockCallback.EVENT.register(BackpackTrinket::tryPlaceBackpack);
        ItemEvents.USE_ON.register(BackpackTrinket::tryPlaceBackpack);
        BuiltInRegistries.ITEM.stream()
               .filter(item -> item instanceof BundleGuiItem)
               .forEach(item -> TrinketCallback.setCallback(item, new BackpackTrinket()));
    }

    public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        if (!Configuration.instance().display_back) return;

        if (entity instanceof ServerPlayer player) {
            CosmeticManager manager = CosmeticManager.manager();
            if (!manager.hasInstance(player)) manager.getOrCreateInstance(player, stack);

            UUID uuid = BackpackUUID.getStackUUID(stack);

            if (uuid == null) return;
            UpgradeContainerComponent component = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
            if (component != null) component.baseUpgrades().forEach((upgrade) ->
                    upgrade.tick(player, stack, player.level(), player.position(), BackpackManager.getInventory(uuid)));
        }
    }

    public void onEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        if (!Configuration.instance().display_back) return;

        if (entity instanceof ServerPlayer player) {
            CosmeticManager.manager().getOrCreateInstance(player, stack);
        }
    }

    public void onUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        if (!Configuration.instance().display_back) return;

        if (entity instanceof ServerPlayer player) {
            CosmeticManager manager = CosmeticManager.manager();

            BackHolder holder = manager.getOrCreateInstance(player, stack);
            holder.destroy();
            manager.removeInstance(player);
        }
    }

    private static boolean canPlaceBackpack(Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean placeWithFullHand = player.getAttachedOrCreate(BackpackPersistentDataAttachmentRegistry.PLACE_WITH_HAND_FULL);
        boolean placeWithFullOffHand = player.getAttachedOrCreate(BackpackPersistentDataAttachmentRegistry.PLACE_WITH_OFFHAND_FULL);

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean isBlockMainItem = player.getMainHandItem().getItem() instanceof BlockItem;
        boolean isBlockOffItem = player.getOffhandItem().getItem() instanceof BlockItem;
        boolean canUseMainHand = mainHand.isEmpty() || (placeWithFullHand && !isBlockMainItem);
        boolean canUseOffHand = offHand.isEmpty() || (placeWithFullOffHand && !isBlockOffItem);

        if (!canUseMainHand || !canUseOffHand) {
            return false;
        }

        ItemStack backpack = getStackInBackSlot(player);

        if (backpack.isEmpty()) {
            return false;
        }

        BundleGuiItem item = (BundleGuiItem) backpack.getItem();
        item.place(new BlockPlaceContext(player, hand, backpack, hitResult));

        return true;
    }

    private static boolean canEquipBackpack(Level world, Player player, BlockPos pos) {
        boolean equipWithFullHand = player.getAttachedOrCreate(BackpackPersistentDataAttachmentRegistry.EQUIP_WITH_HAND_FULL);
        boolean equipWithFullOffHand = player.getAttachedOrCreate(BackpackPersistentDataAttachmentRegistry.EQUIP_WITH_OFFHAND_FULL);

        if (!(world.getBlockState(pos).getBlock() instanceof BasicBackpackBlock backpackBlock)) return false;
        if (!(world.getBlockEntity(pos) instanceof BasicBackpackBlockEntity blockEntity)) return false;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean canUseMainHand = mainHand.isEmpty() || equipWithFullHand;
        boolean canUseOffHand = offHand.isEmpty() || equipWithFullOffHand;

        if (!canUseMainHand || !canUseOffHand) {
            return false;
        }

        return player.isShiftKeyDown()
                && player instanceof ServerPlayer serverPlayer
                && backpackBlock.trinketInteraction(blockEntity, serverPlayer, world, pos);
    }


    public static InteractionResult tryPlaceBackpack(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        if (world.isClientSide() && !Configuration.instance().placeable) return InteractionResult.PASS;
        if (player.isShiftKeyDown() && canPlaceBackpack(player, hand, hitResult)) return InteractionResult.SUCCESS;

        if (canEquipBackpack(world, player, hitResult.getBlockPos()))
            return InteractionResult.SUCCESS;
        else return InteractionResult.PASS;
    }

    public static InteractionResult tryPlaceBackpack(UseOnContext context) {
        Level world = context.getLevel();

        if (world.isClientSide()) return null;

        BlockHitResult hitResult = new BlockHitResult(
                context.getClickLocation(),
                context.getClickedFace(),
                context.getClickedPos(),
                context.isInside()
        );

        Player player = context.getPlayer();
        if (player == null) return null;
        if (player.isShiftKeyDown() && canPlaceBackpack(player, context.getHand(), hitResult)) return InteractionResult.SUCCESS;

        if (canEquipBackpack(world, player, hitResult.getBlockPos()))
            return InteractionResult.SUCCESS;
        else return null;
    }

    public static void equipStack(Player player, ItemStack stack) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        TrinketInventory inventory = attachment.getInventory("chest/back");

        if (inventory == null) return;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack trinket = inventory.getItem(i);
            if (trinket.isEmpty()) {
                inventory.setItem(i, stack);
                break;
            }
        }
    }

    public static boolean isBackSlotOccupied(Player player) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        TrinketInventory inventory = attachment.getInventory("chest/back");

        if (inventory == null) return false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) return true;
        }

        return false;
    }

    public static ItemStack getStackInBackSlot(Player player) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        TrinketInventory inventory = attachment.getInventory("chest/back");

        if (inventory == null) return ItemStack.EMPTY;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) return stack;
        }

        return ItemStack.EMPTY;
    }
}
