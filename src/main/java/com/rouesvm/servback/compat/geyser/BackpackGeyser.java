package com.rouesvm.servback.compat.geyser;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineResourcePacksEvent;
import org.geysermc.geyser.api.pack.PackCodec;
import org.geysermc.geyser.api.pack.ResourcePack;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.rouesvm.servback.Main.MOD_ID;

public class BackpackGeyser implements EventRegistrar {
    public static Path PACKS_FOLDER;
    public static Path GEYSER_PACK;

    static GeyserApi geyser;

    public static void initialize() {
        loadResourcePack();

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            geyser = GeyserApi.api();

            EventRegistrar registrar = new BackpackGeyser();

            geyser.eventBus().register(registrar, registrar);
            geyser.eventBus().subscribe(registrar, GeyserDefineCustomBlocksEvent.class, BackpackGeyserBlock::onGeyserDefineCustomBlocksEvent);
            geyser.eventBus().subscribe(registrar, GeyserDefineCustomItemsEvent.class, BackpackGeyserItem::onGeyserDefineCustomItemsEvent);
        });
    }

    public static void loadResourcePack() {
        PACKS_FOLDER = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + "/");
        GEYSER_PACK = PACKS_FOLDER.resolve("backpack.zip");

        try {
            if (!GEYSER_PACK.toFile().exists()) {
                Files.createDirectories(PACKS_FOLDER);
                Path file = FabricLoader.getInstance().getModContainer(MOD_ID).flatMap(
                        modContainer -> modContainer.findPath("bedrock/backpack.zip")).get();
                Files.copy(file, GEYSER_PACK);

                GEYSER_PACK = PACKS_FOLDER.resolve("backpack.zip");
            }
        } catch (Exception ignored) {}
    }

    @Subscribe
    public void onDefineResource(GeyserDefineResourcePacksEvent event) {
        if (GEYSER_PACK != null && GEYSER_PACK.toFile().exists()) event.register(ResourcePack.create(PackCodec.path(GEYSER_PACK)));
    }

    public static boolean isPlayerOnBedrock(ServerPlayerEntity player) {
        if (geyser == null || player == null) return false;
        return geyser.isBedrockPlayer(player.getUuid());
    }
}
