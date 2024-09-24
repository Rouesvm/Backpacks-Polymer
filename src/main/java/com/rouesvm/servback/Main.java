package com.rouesvm.servback;

import com.rouesvm.servback.items.ItemList;
import com.rouesvm.servback.items.ModItemGroup;
import com.rouesvm.servback.state.StateSaverAndLoader;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class Main implements ModInitializer {
	public static final String MOD_ID = "serverbackpacks";

	public static ItemStack[] globalInventory = new ItemStack[27];

	public static final RegistryKey<Enchantment> CAPACITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "capacity"));

	@Override
	public void onInitialize() {
		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		ItemList.initialize();
		ModItemGroup.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register((server -> {
			StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
			globalInventory = serverState.globalInventory.toArray(ItemStack[]::new);
		}));

		ServerLifecycleEvents.SERVER_STOPPING.register((server -> {
			StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
			DefaultedList<ItemStack> inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
			for (int i = 0; i < 27; i++) {
				ItemStack stack = globalInventory[i];
				if (stack == null)
					stack = ItemStack.EMPTY;

				inventory.set(i, stack);
			}

			serverState.globalInventory = inventory;
		}));
	}

	public static Inventory getInventory() {
		return new SimpleInventory(globalInventory);
	}
}
