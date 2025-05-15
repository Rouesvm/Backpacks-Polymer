package com.rouesvm.servback.compat.geyser;

import com.rouesvm.servback.utils.bedrock.BedrockBlock;
import com.rouesvm.servback.utils.bedrock.BedrockItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.block.custom.NonVanillaCustomBlockData;
import org.geysermc.geyser.api.block.custom.component.BoxComponent;
import org.geysermc.geyser.api.block.custom.component.CustomBlockComponents;
import org.geysermc.geyser.api.block.custom.component.GeometryComponent;
import org.geysermc.geyser.api.block.custom.component.MaterialInstance;
import org.geysermc.geyser.api.block.custom.nonvanilla.JavaBlockState;
import org.geysermc.geyser.api.block.custom.nonvanilla.JavaBoundingBox;
import org.geysermc.geyser.api.event.EventRegistrar;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineResourcePacksEvent;
import org.geysermc.geyser.api.item.custom.NonVanillaCustomItemData;
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

    @Subscribe
    public void onGeyserDefineCustomItemsEvent(GeyserDefineCustomItemsEvent event) {
        Registries.ITEM.getEntrySet().stream()
                .filter(entry -> entry.getValue() instanceof BedrockItem)
                .forEach(entry -> {
                    Item item = entry.getValue();
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
                });
    }

    @Subscribe
    public void onGeyserDefineCustomBlocksEvent(GeyserDefineCustomBlocksEvent event) {
        Registries.BLOCK.getEntrySet().stream()
                .filter(entry -> entry.getValue() instanceof BedrockBlock)
                .forEach(entry -> {
                    Identifier location = entry.getKey().getValue();
                    Block block = entry.getValue();

                    BoxComponent collisionBox = BoxComponent.fullBox();
                    BoxComponent selectionBox = BoxComponent.fullBox();

                    CustomBlockComponents components = CustomBlockComponents.builder()
                            .collisionBox(collisionBox)
                            .selectionBox(selectionBox)
                            .materialInstance("backpack", MaterialInstance.builder()
                                    .texture("serverbackpacks:large")
                                    .renderMethod("opaque")
                                    .faceDimming(true)
                                    .ambientOcclusion(true)
                                    .build())
                            .geometry(GeometryComponent.builder()
                                    .identifier("geometry.backpack")
                                    .build())
                            .lightEmission(block.getDefaultState().getLuminance())
                            .lightDampening(block.getDefaultState().getOpacity())
                            .friction(block.getSlipperiness() / 2)
                            .build();

                    JavaBlockState state = JavaBlockState.builder()
                            .identifier(location.toString())
                            .javaId(Block.getRawIdFromState(block.getDefaultState()))
                            .blockHardness(block.getHardness())
                            .canBreakWithHand(true)
                            .collision(new JavaBoundingBox[]{new JavaBoundingBox(0, 0, 0, 1, 1, 1)})
                            .build();

                    NonVanillaCustomBlockData data = NonVanillaCustomBlockData.builder()
                            .name(location.getPath())
                            .namespace(location.getNamespace())
                            .components(components)
                            .build();

                    event.register(data);
                    event.registerOverride(state, data.defaultBlockState());
                });
    }

    public static boolean isPlayerOnBedrock(ServerPlayerEntity player) {
        if (geyser == null || player == null) return false;
        return geyser.isBedrockPlayer(player.getUuid());
    }
}
