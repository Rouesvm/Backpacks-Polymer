package com.rouesvm.servback.compat.geyser;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.content.item.BundleGuiItem;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import org.geysermc.cumulus.form.SimpleForm;
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
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

@SuppressWarnings("unused")
public class BackpackGeyser implements EventRegistrar {
    public static Path PACKS_FOLDER;
    public static Path GEYSER_PACK;

    private static GeyserApi geyser;

    public static void sendMainForm(ServerPlayer player) {
        SimpleForm.Builder form = SimpleForm.builder()
                .title("§c§c§b")
                .button("Dash\\n§7dash to direction\",\"textures/ui/wind_charged_effect");

        geyser.sendForm(player.getUUID(),
                form.validResultHandler((simpleForm, simpleFormResponse) -> {
                    if (simpleFormResponse.clickedButtonId() == 0) {
                        if (ServerBackpacks.hasTrinketLoaded && BackpackTrinket.isBackSlotOccupied(player)) {
                            Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
                            component.ifPresent(trinketComponent -> trinketComponent.forEach((slotReference, stack) -> {
                                if (stack.getItem() instanceof BundleGuiItem item
                                ) item.onOpenGui(player, stack);
                            }));
                        }
                    }
                    sendMainForm(player);
                }).closedOrInvalidResultHandler((response) -> {
                    sendMainForm(player);
                }).build()
        );
    }

    public static void initialize() {
        loadResourcePack();

        ServerLifecycleEvents.SERVER_STARTING.register(minecraftServer -> {
            geyser = GeyserApi.api();

            EventRegistrar registrar = new BackpackGeyser();

            geyser.eventBus().register(registrar, registrar);
            geyser.eventBus().subscribe(registrar, GeyserDefineCustomBlocksEvent.class, BackpackGeyserBlock::onGeyserDefineCustomBlocksEvent);
            geyser.eventBus().subscribe(registrar, GeyserDefineCustomItemsEvent.class, BackpackGeyserItem::onGeyserDefineCustomItemsEvent);
        });

        ServerPlayerEvents.JOIN.register(BackpackGeyser::sendMainForm);
        ServerPlayerEvents.AFTER_RESPAWN.register((player, a, b) -> sendMainForm(a));

    }

    public static void loadResourcePack() {
        PACKS_FOLDER = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + "/");
        GEYSER_PACK = PACKS_FOLDER.resolve("backpack.zip");

        try {
            if (!PACKS_FOLDER.toFile().exists()) Files.createDirectories(PACKS_FOLDER);
            Path file = FabricLoader.getInstance().getModContainer(MOD_ID).flatMap(
                    modContainer -> modContainer.findPath("bedrock/backpack.zip")).get();
            Files.copy(file, GEYSER_PACK, StandardCopyOption.REPLACE_EXISTING);
            GEYSER_PACK = PACKS_FOLDER.resolve("backpack.zip");
        } catch (Exception ignored) {}
    }

    @Subscribe
    public void onDefineResource(GeyserDefineResourcePacksEvent event) {
        if (GEYSER_PACK != null && GEYSER_PACK.toFile().exists()) event.register(ResourcePack.create(PackCodec.path(GEYSER_PACK)));
    }

    public static boolean isPlayerOnBedrock(ServerPlayer player) {
        if (geyser == null || player == null) return false;
        return geyser.isBedrockPlayer(player.getUUID());
    }
}
