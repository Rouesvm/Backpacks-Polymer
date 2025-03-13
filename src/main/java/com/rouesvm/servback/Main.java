package com.rouesvm.servback;

import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import com.rouesvm.servback.items.ItemRegistry;
import com.rouesvm.servback.items.ModItemGroup;
import com.rouesvm.servback.state.BackpackState;
import com.rouesvm.servback.state.GlobalBackpackState;
import com.rouesvm.servback.utils.BackpackManager;
import com.rouesvm.servback.utils.BaseInventory;
import com.rouesvm.servback.utils.bedrock.GeyserEntry;
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

	public static final boolean hasGeyserLoaded = FabricLoader.getInstance().isModLoaded("geyser-fabric");

	public static BackpackManager backpackManager;

	@Override
	public void onInitialize() {
		backpackManager = new BackpackManager();

		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();

		if (hasGeyserLoaded) GeyserEntry.initialize();

		BackpacksDataComponentTypes.initialize();

		ItemRegistry.initialize();
		ModItemGroup.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register((server -> {
			BackpackState backpackState = BackpackState.getServerState(server);
			GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
			backpackManager.globalInventory = globalBackpackState.globalInventory;
			backpackManager.load(backpackState.storedInventories);
		}));

		ServerLifecycleEvents.SERVER_STOPPING.register((server -> {
			BackpackState backpackState = BackpackState.getServerState(server);
			GlobalBackpackState globalBackpackState = GlobalBackpackState.getServerState(server);
			globalBackpackState.globalInventory = backpackManager.globalInventory;
			backpackState.storedInventories = backpackManager.save();
		}));
	}

	public static BaseInventory getInventory() {
		return backpackManager.globalInventory;
	}

	public static boolean isBedrock(ServerPlayerEntity player) {
		return player != null && hasGeyserLoaded && GeyserEntry.isPlayerOnBedrock(player);
	}
}
