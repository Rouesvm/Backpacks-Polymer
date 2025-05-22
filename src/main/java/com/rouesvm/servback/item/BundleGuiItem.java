package com.rouesvm.servback.item;

import com.rouesvm.servback.ui.DumbBackpackGui;
import com.rouesvm.servback.ui.inventory.BaseInventory;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import static net.minecraft.item.BundleItem.setSelectedStackIndex;

public class BundleGuiItem extends BasicPolymerBlockItem  {
    public BundleGuiItem(String name, Block block) {
        super(name, Items.LEATHER, block);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        var cast = player.raycast(5,0,false);
        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return ActionResult.PASS;
        if (player.isSneaking())
            return ActionResult.PASS;
        if (cast.getType() == HitResult.Type.BLOCK)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        openGui(serverPlayer, stack);
        player.swingHand(hand, true);
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getPlayer() instanceof ServerPlayerEntity serverPlayer))
            return ActionResult.PASS;
        if (serverPlayer.isSneaking())
            return ActionResult.PASS;

        openGui(serverPlayer, context.getStack());
        serverPlayer.swingHand(context.getHand(), true);
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        Inventory inventory = getInventory(serverPlayer, stack);

        if (inventory == null) {
            return false;
        } else {
            ItemStack itemStack = slot.getStack();

            if (!itemStack.getItem().canBeNested()) return false;

            if (clickType == ClickType.LEFT && !itemStack.isEmpty()) {
                if (BaseInventory.canInsert(itemStack, inventory)) {
                    itemStack = BaseInventory.addStack(itemStack, inventory);
                    ContainerItem.playInsertSound(serverPlayer);
                } else {
                    ContainerItem.playInsertFailSound(serverPlayer);
                }

                slot.setStack(itemStack);
                afterChanged(serverPlayer, stack, inventory);
                onContentChanged(player);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.LEFT && otherStack.isEmpty()) {
            setSelectedStackIndex(stack, -1);
        } else {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            Inventory inventory = getInventory(serverPlayer, stack);

            if (!otherStack.getItem().canBeNested()) return false;

            if (inventory != null) {
                if (clickType == ClickType.LEFT && !otherStack.isEmpty()) {
                    if (BaseInventory.canInsert(otherStack, inventory)) {
                        otherStack = BaseInventory.addStack(otherStack, inventory);
                        ContainerItem.playInsertSound(serverPlayer);
                    } else {
                        ContainerItem.playInsertFailSound(serverPlayer);
                    }

                    cursorStackReference.set(otherStack);
                    afterChanged(serverPlayer, stack, inventory);
                    onContentChanged(player);
                    return true;
                } else if (clickType == ClickType.RIGHT) {
                    openGui(serverPlayer, stack);
                    return true;
                } else {
                    setSelectedStackIndex(stack, -1);
                }
            }
        }
        return false;
    }

    public Inventory getInventory(ServerPlayerEntity player, ItemStack stack) {
        return null;
    }

    public void afterChanged(ServerPlayerEntity player, ItemStack stack, Inventory inventory) {
    }

    public void onContentChanged(PlayerEntity user) {
        ScreenHandler screenHandler = user.currentScreenHandler;
        if (screenHandler != null) {
            screenHandler.onContentChanged(user.getInventory());
        }
    }

    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        ContainerItem.playInsertSound(player);
        new DumbBackpackGui(player, stack, getInventory(stack, player));
    }
}
