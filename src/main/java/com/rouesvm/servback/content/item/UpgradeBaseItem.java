package com.rouesvm.servback.content.item;

import com.rouesvm.servback.content.upgrade.BaseUpgrade;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.item.Items;

public class UpgradeBaseItem extends SimplePolymerItem {
    public BaseUpgrade upgrade;

    public UpgradeBaseItem(Settings settings, BaseUpgrade upgrade) {
        super(settings, Items.POISONOUS_POTATO);
        this.upgrade = upgrade;
    }

    public BaseUpgrade getUpgrade() {
        return upgrade;
    }
}
