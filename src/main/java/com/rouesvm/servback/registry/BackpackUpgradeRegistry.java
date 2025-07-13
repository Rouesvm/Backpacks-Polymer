package com.rouesvm.servback.registry;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.UpgradeType;
import com.rouesvm.servback.content.upgrade.impl.CraftingUpgrade;
import com.rouesvm.servback.content.upgrade.impl.MagnetUpgrade;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class BackpackUpgradeRegistry {
    public static final RegistryKey<Registry<UpgradeType<?>>> UPGRADES_REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.of(ServerBackpacks.MOD_ID, "upgrades"));
    public static final Registry<UpgradeType<?>> UPGRADES = FabricRegistryBuilder.createSimple(
            UPGRADES_REGISTRY_KEY).buildAndRegister();

    public static final UpgradeType<MagnetUpgrade> MAGNET = register("magnet", MagnetUpgrade::new);
    public static final UpgradeType<CraftingUpgrade> CRAFTING = register("crafting", CraftingUpgrade::new);

    public static <T extends Upgrade> UpgradeType<T> register(String name, UpgradeType.UpgradeFactory<T> factory) {
        Identifier id = Identifier.of(ServerBackpacks.MOD_ID, name);
        return Registry.register(UPGRADES, id, new UpgradeType<>(id, factory));
    }

    public static UpgradeType<?> get(Identifier id) {
        return UPGRADES.get(id);
    }

    public static void initialize() {}
}
