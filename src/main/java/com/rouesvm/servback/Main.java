package com.rouesvm.servback;

import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.config.Configuration;
import com.rouesvm.servback.config.commands.BackpackCommands;
import com.rouesvm.servback.registry.*;
import com.rouesvm.servback.ui.inventory.BaseInventory;
import com.rouesvm.servback.utils.BackpackManager;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class Main implements ModInitializer {
	public static final String MOD_ID = "serverbackpacks";
	public static final RegistryKey<Enchantment> CAPACITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "capacity"));

	public static boolean hasTrinketLoaded;

	@Override
	public void onInitialize() {
		hasTrinketLoaded = FabricLoader.getInstance().isModLoaded("trinkets");

		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		Configuration.initialize();

		BackpackDataComponentTypes.initialize();

		BackpackBlockEntityRegistry.initialize();
		BackpackBlockRegistry.initialize();

		BackpackItemRegistry.initialize();
		BackpackItemGroup.initialize();

		CommandRegistrationCallback.EVENT.register((dispatcher, a, b) -> BackpackCommands.init(dispatcher));
		if (hasTrinketLoaded) BackpackTrinket.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(BackpackManager::setup);
		ServerLifecycleEvents.SERVER_STOPPING.register(BackpackManager::destroy);
	}

	public static BaseInventory getInventory() {
		return BackpackManager.getManager().globalInventory;
	}
}
