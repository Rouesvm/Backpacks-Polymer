package com.rouesvm.servback;

import com.rouesvm.servback.compat.geyser.BackpackGeyser;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.BackpackRecipeRegistry;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.registry.block.BackpackBlockRegistry;
import com.rouesvm.servback.registry.item.BackpackItemGroup;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.registry.item.BackpackItemRegistry;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.config.commands.BackpackCommands;
import com.rouesvm.servback.technical.data.BackpackManager;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
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

import java.util.Set;

public class ServerBackpacks implements ModInitializer {
	public static final String MOD_ID = "serverbackpacks";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final RegistryKey<Enchantment> CAPACITY = RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(MOD_ID, "capacity"));

	private static final Set<ServerPlayerEntity> BEDROCK_PLAYERS = new ObjectOpenHashSet<>();

	public static boolean hasGeyserLoaded;
	public static boolean hasTrinketLoaded;
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
		if (Configuration.instance().enable_upgrades) BackpackUpgradeRegistry.initialize();

		if (hasGeyserLoaded) BackpackGeyser.initialize();
		if (hasTrinketLoaded) BackpackTrinket.initialize();

		serverEvents();
	}

	private static void serverEvents() {
		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, a, b) -> {
			ServerPlayerEntity player = serverPlayNetworkHandler.getPlayer();
			if (isBedrock(player)) BEDROCK_PLAYERS.add(player);
		});

		ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler, a) ->
				BEDROCK_PLAYERS.remove(serverPlayNetworkHandler.getPlayer()));

		ServerLifecycleEvents.SERVER_STARTING.register(BackpackManager::setup);
		ServerLifecycleEvents.SERVER_STARTED.register(BackpackManager::loadOnServerStarted);

		ServerLifecycleEvents.SERVER_STOPPED.register(BackpackManager::destroy);

		ServerLifecycleEvents.BEFORE_SAVE.register((minecraftServer, b, b1) -> {
			if (BackpackManager.instance() != null) {
				BackpackManager.saveData();
				BackpackManager.createBackup();
			}
		});

		backupEvents();
	}

	private static void backupEvents() {
		ServerPlayerEvents.LEAVE.register((p0) -> BackpackManager.createBackup());
		ServerPlayerEvents.AFTER_RESPAWN.register((p0, p1, p2) -> BackpackManager.createBackup());
	}

	public static boolean isBedrock(ServerPlayerEntity player) {
		return hasGeyserLoaded && player != null && (
						ServerBackpacks.BEDROCK_PLAYERS.contains(player) ||
						BackpackGeyser.isPlayerOnBedrock(player));
	}
}
