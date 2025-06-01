package com.rouesvm.servback;

import com.rouesvm.servback.compat.geyser.BackpackGeyser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Main implements ModInitializer {
	public static final String MOD_ID = "serverbackpacks";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final RegistryKey<Enchantment> CAPACITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "capacity"));

	public static final List<ServerPlayerEntity> BEDROCK_PLAYERS = new ArrayList<>();

	public static boolean hasTrinketLoaded;
	public static boolean hasGeyserLoaded;

	@Override
	public void onInitialize() {
		hasTrinketLoaded = FabricLoader.getInstance().isModLoaded("trinkets");
		hasGeyserLoaded = FabricLoader.getInstance().isModLoaded("geyser-fabric");
		
		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		Configuration.initialize();

		BackpackDataComponentTypes.initialize();

		BackpackBlockEntityRegistry.initialize();
		BackpackBlockRegistry.initialize();

		BackpackItemRegistry.initialize();
		BackpackItemGroup.initialize();

		CommandRegistrationCallback.EVENT.register((dispatcher, a, b) -> BackpackCommands.init(dispatcher));

		if (hasGeyserLoaded) BackpackGeyser.initialize();
		if (hasTrinketLoaded) BackpackTrinket.initialize();

		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, a, b) -> {
			if (isBedrock(serverPlayNetworkHandler.getPlayer())) {
				BEDROCK_PLAYERS.add(serverPlayNetworkHandler.getPlayer());
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, a) ->
				BEDROCK_PLAYERS.remove(serverPlayNetworkHandler.getPlayer()));

		ServerLifecycleEvents.SERVER_STARTING.register(BackpackManager::setup);
		ServerLifecycleEvents.SERVER_STARTED.register(BackpackManager::loadOnServerStarted);

		ServerLifecycleEvents.SERVER_STOPPING.register(BackpackManager::destroy);

		ServerLifecycleEvents.AFTER_SAVE.register((minecraftServer, b, b1) -> {
			if (BackpackManager.instance != null) BackpackManager.save(minecraftServer);
		});
	}

	public static BaseInventory getInventory() {
		return BackpackManager.instance.globalInventory;
	}

	public static boolean isBedrock(ServerPlayerEntity player) {
		return player != null && hasGeyserLoaded && BackpackGeyser.isPlayerOnBedrock(player);
	}
}
