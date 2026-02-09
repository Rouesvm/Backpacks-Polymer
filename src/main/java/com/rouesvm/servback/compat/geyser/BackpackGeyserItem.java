package com.rouesvm.servback.compat.geyser;

import com.rouesvm.servback.compat.geyser.bedrock.BedrockItem;
import com.rouesvm.servback.content.item.BundleGuiItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.item.custom.v2.NonVanillaCustomItemDefinition;
import org.geysermc.geyser.api.item.custom.v2.component.geyser.GeyserBlockPlacer;
import org.geysermc.geyser.api.item.custom.v2.component.geyser.GeyserItemDataComponents;
import org.geysermc.geyser.api.item.custom.v2.component.java.JavaItemDataComponents;

public class BackpackGeyserItem {
    public static void onGeyserDefineCustomItemsEvent(GeyserDefineCustomItemsEvent event) {
        BuiltInRegistries.ITEM.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof BedrockItem)
                .forEach(entry -> {
                    Item item = entry.getValue();
                    int id = BuiltInRegistries.ITEM.getId(item);
                    Identifier identifier = entry.getKey().identifier();
                    String stringIdentifier = identifier.toString();

                    NonVanillaCustomItemDefinition.Builder builder = NonVanillaCustomItemDefinition.builder(
                            org.geysermc.geyser.api.util.Identifier.of(stringIdentifier), id)
                            .component(JavaItemDataComponents.MAX_STACK_SIZE, item.getDefaultMaxStackSize())
                            .displayName(Component.translatable(item.getDescriptionId()).getString());

                    if (item instanceof BundleGuiItem gui) {
                        builder.component(GeyserItemDataComponents.BLOCK_PLACER, GeyserBlockPlacer.builder()
                                .block(org.geysermc.geyser.api.util.Identifier.of(BuiltInRegistries.BLOCK.getKey(gui.getBlock()).toString()))
                                .useBlockIcon(false)
                                .build());
                    }

                    event.register(builder.build());
                });
    }
}
