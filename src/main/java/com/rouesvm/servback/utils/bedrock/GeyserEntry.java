package com.rouesvm.servback.utils.bedrock;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserLoadResourcePacksEvent;
import org.geysermc.geyser.api.item.custom.NonVanillaCustomItemData;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.rouesvm.servback.Main.MOD_ID;

public class GeyserEntry implements EventRegistrar {
    public static Path PACKS_FOLDER;
    public static Path GEYSER_PACK;

    static GeyserApi geyser;

    public static void initialize() {
        loadResourcePack();

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            geyser = GeyserApi.api();

            EventRegistrar registrar = new GeyserEntry();
            geyser.eventBus().register(registrar, registrar);
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
        } catch (Exception e) {
            throw new RuntimeException("PACK doesn't exist!");
        }
    }

    @Subscribe
    public void onDefineResource(GeyserLoadResourcePacksEvent event) {
        if (GEYSER_PACK.toFile().exists()) event.resourcePacks().add(GEYSER_PACK);
    }

    @Subscribe
    public void onGeyserDefineCustomItemsEvent(GeyserDefineCustomItemsEvent event) {
        for (Map.Entry<RegistryKey<Item>, Item> entry : Registries.ITEM.getEntrySet()) {
            var item = entry.getValue();
            if (item instanceof BedrockItem) {
                int id = Registries.ITEM.getRawId(item);
                Identifier identifier = entry.getKey().getValue();

                NonVanillaCustomItemData customItemData = NonVanillaCustomItemData.builder()
                        .displayName(Text.translatable(item.getTranslationKey()).getString())
                        .name(Text.translatable(item.getTranslationKey()).getString())
                        .javaId(id)
                        .stackSize(1)
                        .identifier(identifier.toString())
                        .translationString(item.getTranslationKey())
                        .allowOffhand(true)
                        .displayHandheld(true)
                        .icon(identifier.toString())
                        .creativeCategory(3)
                        .build();
                event.register(customItemData);
            }
        }
    }

    public static boolean isPlayerOnBedrock(ServerPlayerEntity player) {
        if (geyser == null || player == null) return false;
        return geyser.isBedrockPlayer(player.getUuid());
    }
}
