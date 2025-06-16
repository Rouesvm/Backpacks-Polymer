package com.rouesvm.servback.compat.geyser;

import com.rouesvm.servback.compat.geyser.bedrock.BedrockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.item.custom.NonVanillaCustomItemData;

public class BackpackGeyserItem {
    public static void onGeyserDefineCustomItemsEvent(GeyserDefineCustomItemsEvent event) {
        Registries.ITEM.getEntrySet().stream()
                .filter(entry -> entry.getValue() instanceof BedrockItem)
                .forEach(entry -> {
                    Item item = entry.getValue();
                    int id = Registries.ITEM.getRawId(item);
                    Identifier identifier = entry.getKey().getValue();
                    String stringIdentifier = identifier.toString();

                    NonVanillaCustomItemData customItemData = NonVanillaCustomItemData.builder()
                            .displayName(Text.translatable(item.getTranslationKey()).getString())
                            .name(identifier.getPath())
                            .javaId(id)
                            .stackSize(1)
                            .identifier(stringIdentifier)
                            .allowOffhand(true)
                            .displayHandheld(true)
                            .icon(stringIdentifier)
                            .creativeCategory(3)
                            .build();

                    event.register(customItemData);
                });
    }
}
