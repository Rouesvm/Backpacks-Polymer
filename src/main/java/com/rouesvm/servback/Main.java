package com.rouesvm.servback;

import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import com.rouesvm.servback.items.ItemList;
import com.rouesvm.servback.items.ModItemGroup;
import com.rouesvm.servback.state.StateSaverAndLoader;
import com.rouesvm.servback.utils.BackpackManager;
import com.rouesvm.servback.utils.BaseInventory;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class Main implements ModInitializer {
	public static final String MOD_ID = "serverbackpacks";
	public static final RegistryKey<Enchantment> CAPACITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "capacity"));

	public static BackpackManager backpackManager;

	@Override
	public void onInitialize() {
		backpackManager = new BackpackManager();

		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		BackpacksDataComponentTypes.initialize();

		ItemList.initialize();
		ModItemGroup.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register((server -> {
			StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
			backpackManager.globalInventory = serverState.globalInventory;
			backpackManager.loadNbt(serverState.storedInventories);
		}));

		ServerLifecycleEvents.SERVER_STOPPING.register((server -> {
			StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
			serverState.globalInventory = backpackManager.globalInventory;
			serverState.storedInventories = backpackManager.saveNbt();
		}));
	}

	public static BaseInventory getInventory() {
		return backpackManager.globalInventory;
	}
}
