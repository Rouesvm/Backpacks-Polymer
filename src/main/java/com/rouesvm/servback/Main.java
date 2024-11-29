package com.rouesvm.servback;

import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import com.rouesvm.servback.items.ItemList;
import com.rouesvm.servback.items.ModItemGroup;
import com.rouesvm.servback.state.StateSaverAndLoader;
import com.rouesvm.servback.utils.BackpackManager;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class Main implements ModInitializer {
	public static final String MOD_ID = "serverbackpacks";

	public static DefaultedList<ItemStack> globalInventory = DefaultedList.ofSize(27, ItemStack.EMPTY);

	public static final RegistryKey<Enchantment> CAPACITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "capacity"));

	public static final BackpackManager backpackManager = new BackpackManager();

	@Override
	public void onInitialize() {
		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		BackpacksDataComponentTypes.initialize();

		ItemList.initialize();
		ModItemGroup.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register((server -> {
			StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
			globalInventory = serverState.globalInventory;
			backpackManager.load(serverState.storedInventories);
		}));

		ServerLifecycleEvents.SERVER_STOPPING.register((server -> {
			StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
			serverState.globalInventory = globalInventory;
			serverState.storedInventories = backpackManager.save();
		}));
	}

	public static SimpleInventory getInventory() {
		return new SimpleInventory(globalInventory.toArray(ItemStack[]::new));
	}

	public static void setGlobalInventory(SimpleInventory inventory) {
		globalInventory = inventory.getHeldStacks();
	}
}
