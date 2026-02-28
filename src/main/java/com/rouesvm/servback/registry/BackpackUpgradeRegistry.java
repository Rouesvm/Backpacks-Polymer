package com.rouesvm.servback.registry;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.UpgradeType;
import com.rouesvm.servback.content.upgrade.impl.*;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public class BackpackUpgradeRegistry {
    private static final ResourceKey<Registry<UpgradeType<?>>> UPGRADES_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(ServerBackpacks.MOD_ID, "upgrades"));
    private static final Registry<UpgradeType<?>> UPGRADES = FabricRegistryBuilder.createSimple(
            UPGRADES_REGISTRY_KEY).buildAndRegister();

    public static final UpgradeType<VoidUpgrade> VOID = register("void", VoidUpgrade::new);
    public static final UpgradeType<MagnetUpgrade> MAGNET = register("magnet", MagnetUpgrade::new);
    public static final UpgradeType<JukeboxUpgrade> JUKEBOX = register("jukebox", JukeboxUpgrade::new);

    public static final UpgradeType<CraftingUpgrade> CRAFTING = register("crafting", CraftingUpgrade::new);
    public static final UpgradeType<StonecutterUpgrade> STONECUTTER = register("stonecutter", StonecutterUpgrade::new);

    public static <T extends Upgrade> UpgradeType<T> register(String name, UpgradeType.UpgradeFactory<T> factory) {
        Identifier id = Identifier.fromNamespaceAndPath(ServerBackpacks.MOD_ID, name);
        return Registry.register(UPGRADES, id, new UpgradeType<>(id, factory));
    }

    public static Registry<UpgradeType<?>> getRegistry() {
        return UPGRADES;
    }

    public static UpgradeType<?> get(Identifier id) {
        return UPGRADES.getValue(id);
    }

    public static void initialize() {}
}
