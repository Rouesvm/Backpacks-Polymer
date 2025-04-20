package com.rouesvm.servback;

import com.rouesvm.servback.compat.geyser.BackpackGeyser;
import com.rouesvm.servback.config.Configuration;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackItemGroup;
import com.rouesvm.servback.registry.BackpackItemRegistry;
import com.rouesvm.servback.ui.inventory.BaseInventory;
import com.rouesvm.servback.utils.BackpackManager;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class Main implements ModInitializer {
	public static final String MOD_ID = "serverbackpacks";
	public static final RegistryKey<Enchantment> CAPACITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "capacity"));

	public static boolean hasTrinketLoaded;
	public static boolean hasGeyserLoaded;

	public static Configuration configuration;

	@Override
	public void onInitialize() {
		configuration = new Configuration(MOD_ID + ".json");
		configuration.load();

		ServerLifecycleEvents.BEFORE_SAVE.register((s, a, b) -> configuration.save());

		hasTrinketLoaded = FabricLoader.getInstance().isModLoaded("trinkets");
		hasGeyserLoaded = FabricLoader.getInstance().isModLoaded("geyser-fabric");

		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		BackpackDataComponentTypes.initialize();

		BackpackItemRegistry.initialize();
		BackpackItemGroup.initialize();

		if (hasGeyserLoaded) BackpackGeyser.initialize();
		//if (hasTrinketLoaded) BackpackTrinket.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(BackpackManager::setup);
		ServerLifecycleEvents.SERVER_STOPPING.register(BackpackManager::destroy);
	}

	public static BaseInventory getInventory() {
		return BackpackManager.getGlobalInventory();
	}

	public static boolean isBedrock(ServerPlayerEntity player) {
		return player != null && hasGeyserLoaded && BackpackGeyser.isPlayerOnBedrock(player);
	}
}
