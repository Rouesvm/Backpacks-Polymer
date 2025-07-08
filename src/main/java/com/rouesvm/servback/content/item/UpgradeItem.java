package com.rouesvm.servback.content.item;

import com.rouesvm.servback.content.upgrade.Upgrade;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.item.Items;

public class UpgradeItem extends SimplePolymerItem {
    public Upgrade upgrade;

    public UpgradeItem(Settings settings, Upgrade upgrade) {
        super(settings, Items.POISONOUS_POTATO, true);
        this.upgrade = upgrade;
    }

    public Upgrade getUpgrade() {
        return upgrade;
    }
}
