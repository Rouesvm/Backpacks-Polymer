package com.rouesvm.servback.compat.geyser;

import com.rouesvm.servback.compat.geyser.bedrock.BedrockItem;
import com.rouesvm.servback.content.item.BundleGuiItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.item.custom.NonVanillaCustomItemData;

public class BackpackGeyserItem {
    public static void onGeyserDefineCustomItemsEvent(GeyserDefineCustomItemsEvent event) {
        BuiltInRegistries.ITEM.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof BedrockItem)
                .forEach(entry -> {
                    Item item = entry.getValue();
                    int id = BuiltInRegistries.ITEM.getId(item);
                    Identifier identifier = entry.getKey().identifier();
                    String stringIdentifier = identifier.toString();

                    NonVanillaCustomItemData.Builder customItemData = NonVanillaCustomItemData.builder()
                            .displayName(Component.translatable(item.getDescriptionId()).getString())
                            .name(identifier.getPath())
                            .javaId(id)
                            .identifier(stringIdentifier)
                            .allowOffhand(true)
                            .displayHandheld(item instanceof BundleGuiItem)
                            .stackSize(item.getDefaultMaxStackSize())
                            .icon(stringIdentifier)
                            .creativeCategory(3);

                    event.register(customItemData.build());
                });
    }
}
