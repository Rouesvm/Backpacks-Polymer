package com.rouesvm.servback.utils.bedrock;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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
import org.geysermc.geyser.api.item.custom.NonVanillaCustomItemData;

import java.util.Map;

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
