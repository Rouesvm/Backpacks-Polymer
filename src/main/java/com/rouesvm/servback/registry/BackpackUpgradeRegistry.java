package com.rouesvm.servback.registry;

import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.impl.MagnetUpgrade;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class BackpackUpgradeRegistry {
    public static HashMap<Identifier, Upgrade> UPGRADES = new HashMap<>();

    public static Upgrade MAGNET_UPGRADE = register(new MagnetUpgrade());

    public static Upgrade register(Upgrade upgrade) {
        UPGRADES.putIfAbsent(upgrade.id, upgrade);
        return UPGRADES.get(upgrade.id);
    }

    public static void initialize() {};
}
