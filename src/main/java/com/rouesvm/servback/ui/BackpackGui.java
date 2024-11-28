package com.rouesvm.servback.ui;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.slots.BackpackSlot;
import com.rouesvm.servback.slots.DisabledSlot;
import com.rouesvm.servback.utils.BackpackInventory;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.util.Objects;
import java.util.UUID;

public class BackpackGui extends SimpleGui {
    protected final ItemStack stack;
    protected final BackpackInventory inventory;

    protected final SimpleInventory simpleInventory;

    protected final UUID uuid;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, int slots) {
        super(getHandler(slots), player, false);

        stack.set(Main.BOOLEAN_TYPE, true);

        this.uuid = UUID.fromString(stack.get(Main.UUID_TYPE));

        this.stack = stack;

        this.inventory = Main.backpackManager.getInventory(uuid, slots);

        if (stack.get(DataComponentTypes.CONTAINER) != null && stack.getItem() instanceof ContainerItem item) {
            DefaultedList<ItemStack> itemStacks = item.getComponentItemList(stack);
            this.inventory.insertItems(itemStacks);
            stack.set(DataComponentTypes.CONTAINER, null);
        }

        this.simpleInventory = this.inventory.getSimpleInventory();

        this.setTitle(Text.translatable("item.serverbackpacks.gui_backpack"));
        this.fillChest();

        this.open();
        this.afterOpened();
    }

    public void afterOpened() {
        this.getPlayer().currentScreenHandler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stackSlot) {
                inventory.setInventory(simpleInventory.getHeldStacks());
                Main.backpackManager.saveBackpack(uuid, inventory);
            }
            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

            }
        });
    }

    @Override
    public void onTick() {
        if (this.stack.isEmpty())
            this.close();
    }

    public static ScreenHandlerType<?> getHandler(int slots) {
        return switch (slots/9) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> null;
        };
    }

    public void fillChest() {
        for (int j = 0; j < this.inventory.size(); ++j)
            this.setSlotRedirect(j, new BackpackSlot(this.simpleInventory, j, j,0));
    }
}
