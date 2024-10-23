package com.rouesvm.servback;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.items.ItemList;
import com.rouesvm.servback.items.ModItemGroup;
import com.rouesvm.servback.state.StateSaverAndLoader;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class Main implements ModInitializer {
	public static final String MOD_ID = "serverbackpacks";

	public static DefaultedList<ItemStack> globalInventory = DefaultedList.ofSize(27, ItemStack.EMPTY);

	public static final RegistryKey<Enchantment> CAPACITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "capacity"));
	public static final ComponentType<Boolean> BOOLEAN_TYPE = ComponentType.<Boolean>builder().codec(Codec.BOOL).packetCodec(PacketCodecs.BOOL).build();

	@Override
	public void onInitialize() {
		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of(MOD_ID, "boolean"), BOOLEAN_TYPE);
		PolymerComponent.registerDataComponent(BOOLEAN_TYPE);

		ItemList.initialize();
		ModItemGroup.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register((server -> {
			StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
			globalInventory = serverState.globalInventory;
		}));

		ServerLifecycleEvents.SERVER_STOPPING.register((server -> {
			StateSaverAndLoader serverState = StateSaverAndLoader.getServerState(server);
			serverState.globalInventory = globalInventory;
		}));
	}

	public static SimpleInventory getInventory() {
		return new SimpleInventory(globalInventory.toArray(ItemStack[]::new));
	}

	public static void setGlobalInventory(SimpleInventory inventory) {
		globalInventory = inventory.getHeldStacks();
	}
}
