package com.rouesvm.servback;

import com.rouesvm.servback.compat.geyser.BackpackGeyser;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.content.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.content.registry.BackpackRecipeRegistry;
import com.rouesvm.servback.content.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.content.registry.block.BackpackBlockRegistry;
import com.rouesvm.servback.content.registry.item.BackpackItemGroup;
import com.rouesvm.servback.content.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.content.registry.item.BackpackItemRegistry;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.config.commands.BackpackCommands;
import com.rouesvm.servback.technical.data.BackpackManager;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
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

public class ServerBackpacks implements ModInitializer {
	public static final String MOD_ID = "serverbackpacks";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final RegistryKey<Enchantment> CAPACITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "capacity"));

	public static final List<ServerPlayerEntity> BEDROCK_PLAYERS = new ArrayList<>();

	public static boolean hasTrinketLoaded;
	public static boolean hasGeyserLoaded;
	public static boolean isDevEnvironment;

	@Override
	public void onInitialize() {
		isDevEnvironment = FabricLoader.getInstance().isDevelopmentEnvironment();

		hasTrinketLoaded = FabricLoader.getInstance().isModLoaded("trinkets");
		hasGeyserLoaded = FabricLoader.getInstance().isModLoaded("geyser-fabric");

		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		Configuration.initialize();

		BackpackDataComponentTypes.initialize();

		BackpackBlockEntityRegistry.initialize();
		BackpackBlockRegistry.initialize();

		BackpackItemJsonRegistry.initialize();
		BackpackItemRegistry.initialize();
		BackpackItemGroup.initialize();

		BackpackCommands.initialize();

		BackpackRecipeRegistry.initialize();

		if (hasGeyserLoaded) BackpackGeyser.initialize();
		if (hasTrinketLoaded) BackpackTrinket.initialize();

		serverEvents();
	}

	private static void serverEvents() {
		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, a, b) -> {
			if (isBedrock(serverPlayNetworkHandler.getPlayer())) {
				BEDROCK_PLAYERS.add(serverPlayNetworkHandler.getPlayer());
			}
		});

		ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, a) ->
				BEDROCK_PLAYERS.remove(serverPlayNetworkHandler.getPlayer()));

		ServerLifecycleEvents.SERVER_STARTING.register(BackpackManager::setup);
		ServerLifecycleEvents.SERVER_STARTED.register(BackpackManager::loadOnServerStarted);

		ServerLifecycleEvents.SERVER_STOPPED.register(BackpackManager::destroy);

		ServerLifecycleEvents.BEFORE_SAVE.register((minecraftServer, b, b1) -> {
			if (BackpackManager.instance != null) BackpackManager.save(minecraftServer);
		});
	}

	public static boolean isBedrock(ServerPlayerEntity player) {
		return player != null && hasGeyserLoaded && BackpackGeyser.isPlayerOnBedrock(player);
	}
}
