package com.rouesvm.servback.content.item;

import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.UpgradeType;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class UpgradeItem extends SimplePolymerItem {
    public UpgradeType<? extends Upgrade> upgradeType;

    public UpgradeItem(Settings settings, UpgradeType<? extends Upgrade> upgradeType) {
        super(settings, Items.POISONOUS_POTATO, true);
        this.upgradeType = upgradeType;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {
        UpgradeItem upgradeItem = (UpgradeItem) stack.getItem();
        for (Upgrade upgrade : upgradeItem.getUpgradeList(stack)) {
            upgrade.addTooltip(tooltip, stack);
        }
    }

    @Override
    public ItemStack getDefaultStack() {
        ItemStack stack = super.getDefaultStack();

        UpgradeContainerComponent component = new UpgradeContainerComponent(List.of(upgradeType.create()));
        stack.set(BackpackDataComponentTypes.UPGRADE_CONTAINER_COMPONENT_COMPONENT_TYPE, component);

        return stack;
    }

    public List<Upgrade> getUpgradeList(ItemStack stack) {
        UpgradeContainerComponent component = new UpgradeContainerComponent(List.of(upgradeType.create()));
        UpgradeContainerComponent stackComponent = stack.getOrDefault(BackpackDataComponentTypes.UPGRADE_CONTAINER_COMPONENT_COMPONENT_TYPE, component);
        return stackComponent.baseUpgrades;
    }
}
