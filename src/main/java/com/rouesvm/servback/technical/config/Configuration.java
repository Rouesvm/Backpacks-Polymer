package com.rouesvm.servback.technical.config;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import org.joml.Vector3f;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class Configuration {
    public static Configuration manager;

    public static final Instance defaultInstance = new Instance();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configDir;

    private final File configFile;
    public Instance instance = new Instance();

    private final int maxSlots = 9 * 6;

    public static void initialize() {
        manager = new Configuration(MOD_ID + ".json");
        manager.load();

        ServerLifecycleEvents.BEFORE_SAVE.register((a, c, b) -> manager.save());
    }
    
    public Configuration(String name) {
        configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + "/");
        try {
            if (!configDir.toFile().exists()) {
                Files.createDirectories(configDir);
            }
        } catch (IOException ignored) {}

        configFile = configDir.resolve(name).toFile();
        if (!configFile.exists()) save();
    }

    public static Instance instance() {
        return manager.instance;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(instance, writer);
        } catch (IOException ignored) {}
    }

    public void load() {
        try (FileReader reader = new FileReader(configFile)) {
            JsonElement rawJson = JsonParser.parseReader(reader);
            JsonObject jsonObject = rawJson.getAsJsonObject();

            Instance loaded = GSON.fromJson(rawJson, Instance.class);
            if (loaded != null) {
                sanitizeConfig(jsonObject);
                replaceEntryIfInvalid();
                instance = loaded;
            }
        } catch (JsonIOException | JsonSyntaxException | IOException ignored) {}
    }

    public void sanitizeConfig(JsonObject jsonObject) {
        if (jsonObject.has("enable_globalpack") && !jsonObject.get("enable_globalpack").getAsBoolean()) {
            instance.disabled_backpacks.add("global");
        }

        if (jsonObject.has("enable_enderpack") && !jsonObject.get("enable_enderpack").getAsBoolean()) {
            instance.disabled_backpacks.add("ender");
        }
    }

    public void replaceEntryIfInvalid() {
        instance.types_of_backpacks.replaceAll((key, value) -> {
            boolean invalid = value.slots > maxSlots || value.backpacks == null || value.dyeBlacklist == null;
            if (invalid) return defaultInstance.types_of_backpacks.getOrDefault(
                    key,
                    new BackpackType(
                            key * 9,
                            true,
                            value.backpacks != null ? value.backpacks : List.of("unknown"),
                            List.of("brown")
                    )
            );
            else return value;
        });
    }

    public static boolean isDisabled(Item item) {
        String idString = item.toString();
        String removeNamespace = idString.replace(MOD_ID + ":", "");

        if ((removeNamespace.contains("upgrade")
                && !Configuration.instance().enable_upgrades)
        ) return true;

        return Configuration.instance().disabled_backpacks.contains(removeNamespace)
                || Configuration.instance().disabled_upgrades.contains(removeNamespace);
    }

    public static <K, V> LinkedHashMap<K, V> createMap(Map<K, V> map) {
        return new LinkedHashMap<>(map);
    }

    public record BackpackType(int slots, boolean dyeable, List<String> backpacks, List<String> dyeBlacklist) {}

    public static class Instance {
        @SerializedName("types_of_backpacks")
        public final Map<Integer, BackpackType> types_of_backpacks = createMap(Map.of(
                1, new BackpackType(
                        9,
                        true,
                        List.of("small"),
                        List.of("brown")
                ),
                2, new BackpackType(
                        18,
                        true,
                        List.of("medium"),
                        List.of("brown")
                ),
                3, new BackpackType(
                        27,
                        true,
                        List.of("large"),
                        List.of("brown")
                )
        ));

        @SerializedName("disabled_backpacks")
        public final List<String> disabled_backpacks = List.of();

        @SerializedName("enable_upgrades")
        public final boolean enable_upgrades = true;

        @SerializedName("disabled_upgrades")
        public final List<String> disabled_upgrades = List.of();


        @SerializedName("allow_backups")
        public final boolean allow_backups = true;

        @SerializedName("breaks_with_flow")
        public final boolean breaks_with_flow = true;

        @SerializedName("display_back")
        public final boolean display_back = true;

        @SerializedName("back_positions")
        public final Map<Integer, Vector3f> back_positions = createMap(Map.of(
                1, new Vector3f(0, -0.45f, 0.280f),
                2, new Vector3f(0, -0.65f, 0.280f),
                3, new Vector3f(0, -0.65f, 0.280f)
        ));

        @SerializedName("back_yaw")
        public final Map<Integer, Integer> back_yaw = createMap(Map.of(
                1, 180,
                2, 180,
                3, 180
        ));

        @SerializedName("back_pitch_when_sneaking")
        public final Map<Integer, Integer> back_pitch_when_sneaking = createMap(Map.of(
                1, -25,
                2, -25,
                3, -25
        ));
    }
}
