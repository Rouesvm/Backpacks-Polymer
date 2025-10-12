package com.rouesvm.servback.technical.config;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;

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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File configFile;
    public Instance instance = new Instance();

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
    }
}
