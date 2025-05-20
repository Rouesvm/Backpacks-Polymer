package com.rouesvm.servback.item;

import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import com.rouesvm.servback.utils.BackpackManager;
import com.rouesvm.servback.utils.BackpackUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.DyeColor;

import java.util.UUID;

import static net.minecraft.item.BundleItem.setSelectedStackIndex;

public class BundleContainerItem extends ContainerItem {
    public BundleContainerItem(String name, int slots, DyeColor color) {
        super(name, slots, color);
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        UUID uuid = BackpackManager.getStackUUID(stack);
        BackpackInventory inventory = BackpackUtils.getItemList(stack, BackpackUtils.getExtendedSlots(stack) + this.slots);

        if (inventory == null) {
            return false;
        } else {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            ItemStack itemStack = slot.getStack();

            if (!itemStack.getItem().canBeNested()) return false;

            if (clickType == ClickType.LEFT && !itemStack.isEmpty()) {
                if (inventory.canInsert(itemStack)) {
                    itemStack = inventory.addStack(itemStack);
                    playInsertSound(serverPlayer);
                } else {
                    playInsertFailSound(serverPlayer);
                }

                slot.setStack(itemStack);
                BackpackManager.getManager().saveBackpack(uuid, inventory);
                this.onContentChanged(player);
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
            UUID uuid = BackpackManager.getStackUUID(stack);
            BackpackInventory inventory = BackpackUtils.getItemList(stack, BackpackUtils.getExtendedSlots(stack) + this.slots);
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;

            if (!otherStack.getItem().canBeNested()) return false;

            if (inventory != null) {
                if (clickType == ClickType.LEFT && !otherStack.isEmpty()) {
                    if (inventory.canInsert(otherStack)) {
                        otherStack = inventory.addStack(otherStack);
                        playInsertSound(serverPlayer);
                    } else {
                        playInsertFailSound(serverPlayer);
                    }

                    cursorStackReference.set(otherStack);
                    BackpackManager.getManager().saveBackpack(uuid, inventory);
                    this.onContentChanged(serverPlayer);
                    return true;
                } else if (clickType == ClickType.RIGHT) {
                    BackpackUtils.checkEnchantments(stack, serverPlayer, this.slots, BackpackUtils.getExtendedSlots(stack));
                    new BackpackGui(
                            serverPlayer,
                            stack,
                            BackpackManager.getInstance(
                                    BackpackManager.getStackUUID(stack),
                                    BackpackUtils.getExtendedSlots(stack) + this.slots
                            )
                    );
                    return true;
                } else {
                    setSelectedStackIndex(stack, -1);
                }
            }
        }
        return false;
    }

    private void onContentChanged(PlayerEntity user) {
        ScreenHandler screenHandler = user.currentScreenHandler;
        if (screenHandler != null) {
            screenHandler.onContentChanged(user.getInventory());
        }
    }
}
