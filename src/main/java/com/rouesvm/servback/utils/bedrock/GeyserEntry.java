package com.rouesvm.servback.utils.bedrock;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserLoadResourcePacksEvent;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.rouesvm.servback.Main.MOD_ID;

public class GeyserEntry implements EventRegistrar {
    private static GeyserApi geyser;

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            geyser = GeyserApi.api();

            EventRegistrar registrar = new GeyserEntry();
            geyser.eventBus().register(registrar, registrar);
            geyser.eventBus().register(registrar, new GeyserItem());
        });
    }

    @Subscribe
    public void onGeyserLoadResourcePacksEvent(@NotNull GeyserLoadResourcePacksEvent event) {
        Path resource = FabricLoader.getInstance().getModContainer(MOD_ID).get().findPath("bedrock/backpack.zip").get();
        if (Files.exists(resource))
            event.resourcePacks().add(resource);
    }

    public static boolean isPlayerOnBedrock(ServerPlayerEntity player) {
        if (geyser == null || player == null) return false;
        return geyser.isBedrockPlayer(player.getUuid());
    }
}
