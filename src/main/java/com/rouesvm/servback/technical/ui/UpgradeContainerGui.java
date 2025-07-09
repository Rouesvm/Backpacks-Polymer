package com.rouesvm.servback.technical.ui;

import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.UpgradeType;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class UpgradeContainerGui extends SimpleGui {
    protected List<Upgrade> upgradeList;

    public UpgradeContainerGui(ServerPlayerEntity player, ItemStack stack) {
        super(ScreenHandlerType.HOPPER, player, false);

        UpgradeContainerComponent component = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
        if (component == null) return;

        this.upgradeList = component.baseUpgrades;

        this.setTitle(Text.translatable("tooltip.serverbackpacks.upgrades"));

        int i=0;
        for (Upgrade upgrade : upgradeList) {
            UpgradeType<? extends Upgrade> upgradeType = upgrade.getType();
            if (upgradeType == null || upgradeType.getItem() == null) continue;

            Item item = upgradeType.getItem();
            ItemStack upgradeStack = item.getDefaultStack();
            upgradeStack.set(BackpackDataComponentTypes.UPGRADE_CONTAINER, UpgradeContainerComponent.of(List.of(upgrade)));

            this.setSlot(i, upgradeStack);
            i++;
        }

        open();
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {

        return super.onAnyClick(index, type, action);
    }
}
