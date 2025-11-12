package com.rouesvm.servback.technical.ui;

import com.rouesvm.servback.content.component.UpgradeComponent;
import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.UpgradeItem;
import com.rouesvm.servback.content.upgrade.ClickableUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.UpgradeType;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.List;

public class UpgradeContainerGui extends SimpleGui {
    protected List<Upgrade> upgradeList;
    protected final ItemStack backpackStack;

    protected boolean remove = false;

    public static ItemStack modeStack = Items.COMMAND_BLOCK.getDefaultStack();
    static {
        modeStack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("info.serverbackpacks.mode")
                .append(": ")
                .append(Text.translatable("info.serverbackpacks.use")));
    }

    public static ItemStack modeRemoveStack = Items.REPEATING_COMMAND_BLOCK.getDefaultStack();
    static {
        modeRemoveStack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("info.serverbackpacks.mode")
                .append(": ")
                .append(Text.translatable("info.serverbackpacks.remove")));
    }

    private void clickCallback(ClickType clickType) {
        if (clickType.isLeft) {
            this.remove = !remove;
            this.setSlot(4, GuiElementBuilder.from(remove ? modeRemoveStack : modeStack).setCallback(this::clickCallback));
            this.player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.UI, 1, 1);
        }
    }

    public UpgradeContainerGui(ServerPlayerEntity player, ItemStack stack) {
        super(ScreenHandlerType.HOPPER, player, false);

        this.backpackStack = stack;

        UpgradeContainerComponent component = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
        if (component == null) return;

        this.upgradeList = component.getBaseUpgrades();

        this.setTitle(Text.translatable("info.serverbackpacks.upgrades"));

        int i;
        for (i=0; i < upgradeList.size(); i++) {
            Upgrade upgrade = upgradeList.get(i);
            UpgradeType<? extends Upgrade> upgradeType = upgrade.getType();
            if (upgradeType == null || upgradeType.getItem() == null) continue;

            Item item = upgradeType.getItem();
            ItemStack upgradeStack = item.getDefaultStack();
            upgradeStack.set(BackpackDataComponentTypes.UPGRADE, UpgradeComponent.of(upgrade));

            this.setSlot(i, upgradeStack);
        }

        ItemStack barrier = Items.BARRIER.getDefaultStack();
        barrier.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("info.serverbackpacks.empty"));

        for (int index = i; index < getSize(); index++) {
            this.setSlot(index, barrier);
        }

        this.setSlot(4, GuiElementBuilder.from(modeStack).setCallback(this::clickCallback));

        open();
    }


    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        if (index < 0 || index >= this.size) return true;
        Slot slot = this.screenHandler.getSlot(index);
        if (!slot.hasStack()) return true;

        ItemStack stack = slot.getStack();
        if (!(stack.getItem() instanceof UpgradeItem item)) return true;

        Upgrade upgrade = item.getUpgrade(stack);

        if (remove) {
            this.screenHandler.setCursorStack(stack.copyAndEmpty());

            ItemStack barrier = Items.BARRIER.getDefaultStack();
            barrier.set(DataComponentTypes.CUSTOM_NAME, Text.translatable("info.serverbackpacks.empty"));

            this.setSlot(index, barrier);

            upgradeList.remove(upgrade);
            backpackStack.set(BackpackDataComponentTypes.UPGRADE_CONTAINER, UpgradeContainerComponent.of(upgradeList));

            if (upgradeList.isEmpty()) {
                backpackStack.remove(BackpackDataComponentTypes.UPGRADE_CONTAINER);
                close();
            }
        } else {
            if (upgrade instanceof ClickableUpgrade clickableUpgrade)
                clickableUpgrade.onClicked(player, stack, slot, type, true);
        }

        return true;
    }
}
