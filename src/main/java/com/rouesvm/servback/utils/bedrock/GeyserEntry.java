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
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static com.rouesvm.servback.Main.MOD_ID;

public class GeyserEntry implements EventRegistrar {
    static GeyserApi geyser;

    public static void initialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            geyser = GeyserApi.api();

            EventRegistrar registrar = new GeyserEntry();
            geyser.eventBus().register(registrar, registrar);
        });
    }

    @Subscribe
    public void onGeyserDefineCustomItemsEvent(GeyserDefineCustomItemsEvent event) {
        for (Map.Entry<RegistryKey<Item>, Item> entry : Registries.ITEM.getEntrySet()) {
            var item = entry.getValue();
            if (item instanceof BedrockItem) {
                int id = Registries.ITEM.getRawId(item);
                Identifier identifier = entry.getKey().getValue();

                NonVanillaCustomItemData customItemData = NonVanillaCustomItemData.builder()
                        .name(Text.translatable(item.getTranslationKey()).getString())
                        .javaId(id)
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

    @Subscribe
    public void onGeyserLoadResourcePacksEvent(@NotNull GeyserLoadResourcePacksEvent event) {
        Optional<Path> resource = FabricLoader.getInstance().getModContainer(MOD_ID).flatMap(modContainer -> modContainer.findPath("bedrock/backpack.zip"));
        resource.ifPresent(path -> event.resourcePacks().add(path));
    }

    public static boolean isPlayerOnBedrock(ServerPlayerEntity player) {
        if (geyser == null || player == null) return false;
        return geyser.isBedrockPlayer(player.getUuid());
    }
}
