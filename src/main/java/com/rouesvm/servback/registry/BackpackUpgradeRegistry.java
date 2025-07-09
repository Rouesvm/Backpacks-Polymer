package com.rouesvm.servback.registry;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.UpgradeType;
import com.rouesvm.servback.content.upgrade.impl.MagnetUpgrade;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class BackpackUpgradeRegistry {
    private static final Map<Identifier, UpgradeType<?>> REGISTRY = new HashMap<>();

    public static final UpgradeType<MagnetUpgrade> MAGNET = register("magnet", BackpackItemRegistry.MAGNET_UPGRADE, MagnetUpgrade::new);

    public static <T extends Upgrade> UpgradeType<T> register(String name, Item item, UpgradeType.UpgradeFactory<T> factory) {
        Identifier id = Identifier.of(ServerBackpacks.MOD_ID, name);
        UpgradeType<T> type = new UpgradeType<>(id, item, factory);
        REGISTRY.put(id, type);
        return type;
    }

    public static UpgradeType<?> get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static void initialize() {}
}
