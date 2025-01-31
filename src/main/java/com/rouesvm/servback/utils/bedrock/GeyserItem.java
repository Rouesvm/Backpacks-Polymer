package com.rouesvm.servback.utils.bedrock;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.item.custom.NonVanillaCustomItemData;

public class GeyserItem implements Extension {
    @Subscribe
    public void onDefineCustomItem(GeyserDefineCustomItemsEvent event) {
        for (Item item : Registries.ITEM) {
            if (item instanceof BedrockItem bedrockItem) {
                int id = Registries.ITEM.getRawId(item);

                NonVanillaCustomItemData customItemData = NonVanillaCustomItemData.builder()
                        .name(bedrockItem.getIdentifier().getPath())
                        .translationString(bedrockItem.getIdentifier().toTranslationKey("item"))
                        .allowOffhand(true)
                        .displayHandheld(true)
                        .javaId(id)
                        .icon(bedrockItem.getIdentifier().toString())
                        .creativeCategory(1)
                        .build();
                event.register(customItemData);
            }
        }
    }
}
