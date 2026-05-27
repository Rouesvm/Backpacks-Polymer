package com.rouesvm.servback.content.item;

import com.rouesvm.servback.content.block.BasicBackpackBlockEntity;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.ui.BasicInventoryGui;
import com.rouesvm.servback.technical.ui.UpgradeContainerGui;
import com.rouesvm.servback.technical.ui.inventory.BaseInventory;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BundleGuiItem extends BasicPolymerBlockItem  {
    public BundleGuiItem(String name, Block block) {
        super(name, Items.LEATHER, block);
    }

    @Override
    public void modifyClientTooltip(List<Component> tooltip, ItemStack stack, PacketContext context) {
        if (Configuration.isDisabled(stack.getItem())
        ) tooltip.add(Component.translatable("tooltip.serverbackpacks.disabled")
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.RED));
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
            @NonNull BlockPos pos,
            Level world,
            @Nullable Player player,
            @NonNull ItemStack stack,
            @NonNull BlockState state
    ) {
        if (world.getBlockEntity(pos) instanceof BasicBackpackBlockEntity blockEntity) {
            blockEntity.setItem(this);
            blockEntity.setSize(BackpackUtils.getExtendedSlots(stack));

            if (stack.getCustomName() != null
            ) blockEntity.setCustomName(stack.getCustomName());

            blockEntity.setChanged();
            ContainerItem.playOpenSound((ServerPlayer) player);
        }

        return updateCustomBlockEntityTag(world, player, pos, stack);
    }


    @Override
    public @NonNull InteractionResult use(@NonNull Level world, Player player, @NonNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        var cast = player.pick(5,0,false);
        if (!(player instanceof ServerPlayer serverPlayer)
        ) return InteractionResult.PASS;

        if (cast.getType() == HitResult.Type.BLOCK
        ) return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (player.isShiftKeyDown()) {
            if (stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER) != null) {
                new UpgradeContainerGui(serverPlayer, stack);
                return InteractionResult.SUCCESS;
            } else return InteractionResult.PASS;
        }
        
        onOpenGui(serverPlayer, stack);
        player.swing(hand, true);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean canPlace(@NonNull BlockPlaceContext context, @NonNull BlockState state) {
        return Configuration.instance().placeable && super.canPlace(context, state);
    }

    @Override
    public @NonNull InteractionResult useOn(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer serverPlayer)
        ) return InteractionResult.PASS;
        if (Configuration.instance().placeable
                && serverPlayer.isShiftKeyDown()
        ) return super.useOn(context);

        onOpenGui(serverPlayer, context.getItemInHand());
        serverPlayer.swing(context.getHand(), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickType, Player player) {
        ServerPlayer serverPlayer = (ServerPlayer) player;
        Container inventory = getInventory(serverPlayer, stack);

        if (inventory != null) {
            ItemStack itemStack = slot.getItem();

            if (slot instanceof ResultSlot) return false;
            if (!itemStack.getItem().canFitInsideContainerItems()) return false;

            if (clickType == ClickAction.PRIMARY && !itemStack.isEmpty()) {
                if (BaseInventory.canInsert(itemStack, inventory)) {
                    itemStack = BaseInventory.addStack(itemStack, inventory);
                    ContainerItem.playInsertSound(serverPlayer, 0.8F);
                } else {
                    ContainerItem.playInsertFailSound(serverPlayer);
                    return false;
                }

                slot.setByPlayer(itemStack);
                afterChanged(stack, inventory);
                onContentChanged(player);
                return true;
            } else return false;
        } else return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(
            @NonNull ItemStack stack,
            @NonNull ItemStack otherStack,
            @NonNull Slot slot,
            @NonNull ClickAction clickType,
            @NonNull Player player,
            @NonNull SlotAccess cursorStackReference
    ) {
        if (clickType == ClickAction.PRIMARY && otherStack.isEmpty()) {
        } else {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            Container inventory = getInventory(serverPlayer, stack);

            if (slot instanceof ResultSlot) return false;
            if (!otherStack.getItem().canFitInsideContainerItems()) return false;

            if (clickType == ClickAction.SECONDARY) {
                serverPlayer.closeContainer();
                onOpenGui(serverPlayer, stack);
                serverPlayer.inventoryMenu.resumeRemoteUpdates();
                serverPlayer.inventoryMenu.broadcastChanges();
                return true;
            }

            if (inventory != null) {
                if (clickType == ClickAction.PRIMARY && !otherStack.isEmpty()) {
                    if (BaseInventory.canInsert(otherStack, inventory)) {
                        otherStack = BaseInventory.addStack(otherStack, inventory);
                        ContainerItem.playInsertSound(serverPlayer, 0.8F);
                    } else {
                        ContainerItem.playInsertFailSound(serverPlayer);
                        return false;
                    }

                    cursorStackReference.set(otherStack);
                    afterChanged(stack, inventory);
                    onContentChanged(player);
                    return true;
                }
            }
        }
        return false;
    }

    public Container getInventory(@Nullable ServerPlayer player, @Nullable ItemStack stack) {
        return null;
    }

    public void afterChanged(ItemStack stack, Container inventory) {
    }

    public void onContentChanged(Player user) {
        AbstractContainerMenu screenHandler = user.containerMenu;
        screenHandler.slotsChanged(user.getInventory());
    }

    public void onOpenGui(ServerPlayer player, ItemStack stack) {
        ContainerItem.playOpenSound(player);
        openGui(player, stack);
    }

    public void openGui(ServerPlayer player, ItemStack stack) {
        new BasicInventoryGui(player, stack, getInventory(player, stack));
    }
}
