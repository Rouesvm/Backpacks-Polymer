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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class UpgradeContainerGui extends SimpleGui {
    protected List<Upgrade> upgradeList;
    protected final ItemStack backpackStack;

    protected boolean remove = false;

    public static ItemStack modeStack = Items.COMMAND_BLOCK.getDefaultInstance();
    static {
        modeStack.set(DataComponents.CUSTOM_NAME, Component.translatable("info.serverbackpacks.mode")
                .append(": ")
                .append(Component.translatable("info.serverbackpacks.use")));
    }

    public static ItemStack modeRemoveStack = Items.REPEATING_COMMAND_BLOCK.getDefaultInstance();
    static {
        modeRemoveStack.set(DataComponents.CUSTOM_NAME, Component.translatable("info.serverbackpacks.mode")
                .append(": ")
                .append(Component.translatable("info.serverbackpacks.remove")));
    }

    private void clickCallback(ClickType clickType) {
        if (clickType.isLeft) {
            this.remove = !remove;
            this.setSlot(4, GuiElementBuilder.from(remove ? modeRemoveStack : modeStack).setCallback(this::clickCallback));
            this.player.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 1, 1);
        }
    }

    public UpgradeContainerGui(ServerPlayer player, ItemStack stack) {
        super(MenuType.HOPPER, player, false);

        this.backpackStack = stack;

        UpgradeContainerComponent component = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
        if (component == null) return;

        this.upgradeList = component.baseUpgrades();

        this.setTitle(Component.translatable("info.serverbackpacks.upgrades"));

        int i;
        for (i=0; i < upgradeList.size(); i++) {
            Upgrade upgrade = upgradeList.get(i);
            UpgradeType<? extends Upgrade> upgradeType = upgrade.getType();
            if (upgradeType == null || upgradeType.getItem() == null) continue;

            Item item = upgradeType.getItem();
            ItemStack upgradeStack = item.getDefaultInstance();
            upgradeStack.set(BackpackDataComponentTypes.UPGRADE, UpgradeComponent.of(upgrade));

            this.setSlot(i, upgradeStack);
        }

        ItemStack barrier = Items.BARRIER.getDefaultInstance();
        barrier.set(DataComponents.CUSTOM_NAME, Component.translatable("info.serverbackpacks.empty"));

        for (int index = i; index < getSize(); index++) {
            this.setSlot(index, barrier);
        }

        this.setSlot(4, GuiElementBuilder.from(modeStack).setCallback(this::clickCallback));

        open();
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, ContainerInput action) {
        if (index < 0 || index >= this.size) return true;
        Slot slot = this.wrappedMenu.getSlot(index);
        if (!slot.hasItem()) return true;

        ItemStack stack = slot.getItem();
        if (!(stack.getItem() instanceof UpgradeItem item)) return true;

        Upgrade upgrade = item.getUpgrade(stack);

        if (remove && this.wrappedMenu.getCarried().isEmpty()) {
            this.wrappedMenu.setCarried(stack.copyAndClear());

            ItemStack barrier = Items.BARRIER.getDefaultInstance();
            barrier.set(DataComponents.CUSTOM_NAME, Component.translatable("info.serverbackpacks.empty"));

            this.setSlot(index, barrier);

            upgradeList.remove(upgrade);
            backpackStack.set(BackpackDataComponentTypes.UPGRADE_CONTAINER, UpgradeContainerComponent.of(upgradeList));

            if (upgradeList.isEmpty()) {
                backpackStack.remove(BackpackDataComponentTypes.UPGRADE_CONTAINER);
                close();
            }
        } else {
            if (upgrade instanceof ClickableUpgrade clickableUpgrade)
                clickableUpgrade.onClicked(player, stack, wrappedMenu.getCarried(), slot, type, true);
        }

        return true;
    }
}
