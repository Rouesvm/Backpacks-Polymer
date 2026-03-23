package com.rouesvm.servback.technical.config;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.Item;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class Configuration {
    public static Configuration manager;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File configFile;
    public Instance instance = new Instance();

    public List<BackpackType> backpackTypes = new ArrayList<>();

    public static void initialize() {
        manager = new Configuration(MOD_ID + ".json");
        manager.load();

        ServerLifecycleEvents.BEFORE_SAVE.register((a, c, b) -> manager.save());
    }
    
    public Configuration(String name) {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + "/");
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
                convertTypesToList(jsonObject);
                convertOldTypeToList(jsonObject);
                instance = loaded;
            }
        } catch (JsonIOException | JsonSyntaxException | IOException ignored) {}
    }

    public void convertTypesToList(JsonObject jsonObject) {
        if (!jsonObject.has("types_of_backpacks")) return;

        JsonObject types = jsonObject.getAsJsonObject("types_of_backpacks");

        for (String keyStr : types.keySet()) {
            JsonObject oldType = types.getAsJsonObject(keyStr);
            if (oldType == null) continue;

            int key = Integer.parseInt(keyStr);

            int slots = oldType.has("slots") ? oldType.get("slots").getAsInt() : 9;
            boolean dyeable = oldType.has("dyeable") && oldType.get("dyeable").getAsBoolean();

            List<String> backpacks = new ArrayList<>();
            if (oldType.has("backpacks")) {
                oldType.getAsJsonArray("backpacks").forEach((string) -> backpacks.add(string.getAsString()));
            }

            List<String> dyeBlacklist = new ArrayList<>();
            if (oldType.has("dyeBlacklist")) {
                oldType.getAsJsonArray("dyeBlacklist").forEach(e -> dyeBlacklist.add(e.getAsString()));
            }

            if (backpacks.isEmpty()) continue;

            backpackTypes.add(new BackpackType(slots, dyeable, backpacks, dyeBlacklist, key));
        }
    }

    public void convertOldTypeToList(JsonObject jsonObject) {
        if (!jsonObject.has("types_of_backpacks")) return;

        JsonObject types = jsonObject.getAsJsonObject("types_of_backpacks");

        for (String keyStr : types.keySet()) {
            JsonObject oldType = types.getAsJsonObject(keyStr);
            if (oldType == null) continue;

            int key = Integer.parseInt(keyStr);

            int slots = oldType.has("slots") ? oldType.get("slots").getAsInt() : 9;
            boolean dyeable = oldType.has("dyeable") && oldType.get("dyeable").getAsBoolean();

            List<String> backpacks = new ArrayList<>();
            if (oldType.has("name")) {
                backpacks.add(oldType.get("name").getAsString());
            }

            List<String> dyeBlacklist = new ArrayList<>();
            if (oldType.has("dyeBlacklist")) {
                oldType.getAsJsonArray("dyeBlacklist").forEach(e -> dyeBlacklist.add(e.getAsString()));
            }

            if (backpacks.isEmpty()) continue;

            backpackTypes.add(new BackpackType(slots, dyeable, backpacks, dyeBlacklist, key));
        }
    }

    public void sanitizeConfig(JsonObject jsonObject) {
        if (jsonObject.has("enable_globalpack") && !jsonObject.get("enable_globalpack").getAsBoolean()) {
            instance.disabled_backpacks.add("global");
        }

        if (jsonObject.has("enable_enderpack") && !jsonObject.get("enable_enderpack").getAsBoolean()) {
            instance.disabled_backpacks.add("ender");
        }
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

    public record BackpackType(int slots, boolean dyeable, List<String> backpacks, List<String> dyeBlacklist, int tier) {}

    public static class Instance {
        @SerializedName("disabled_backpacks")
        public List<String> disabled_backpacks = List.of();

        @SerializedName("enable_upgrades")
        public boolean enable_upgrades = true;

        @SerializedName("disabled_upgrades")
        public List<String> disabled_upgrades = List.of();

        @SerializedName("placeable")
        public boolean placeable = true;

        @SerializedName("allow_backups")
        public boolean allow_backups = true;

        @SerializedName("breaks_with_flow")
        public boolean breaks_with_flow = true;

        @SerializedName("display_back")
        public boolean display_back = true;

        @SerializedName("allow_skulker_boxes_in_backpacks")
        public boolean allow_shulker_boxes_in_backpacks = false;

        @SerializedName("global_backpack_size")
        public int global_backpack_size = 54;

        @SerializedName("lava_backpack_size")
        public int lava_backpack_size = 18;
    }
}
