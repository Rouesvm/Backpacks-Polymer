package com.rouesvm.servback.registry;

import com.rouesvm.servback.content.upgrade.BaseUpgrade;
import com.rouesvm.servback.content.upgrade.impl.MagnetUpgrade;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class BackpackUpgradeRegistry {
    public static HashMap<Identifier, BaseUpgrade> UPGRADES = new HashMap<>();

    public static BaseUpgrade MAGNET_UPGRADE = register(new MagnetUpgrade());

    public static BaseUpgrade register(BaseUpgrade upgrade) {
        UPGRADES.putIfAbsent(upgrade.id, upgrade);
        return UPGRADES.get(upgrade.id);
    }

    public static void initialize() {};
}
