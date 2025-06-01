package com.rouesvm.servback.technical.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import org.joml.Vector3f;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class Configuration {
    public static Configuration manager;

    public static final Instance defaultInstance = new Instance();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File configFile;
    public Instance instance = new Instance();

    private final int maxSlots = 9 * 6;

    public static void initialize() {
        manager = new Configuration(MOD_ID + ".json");
        manager.load();

        ServerLifecycleEvents.BEFORE_SAVE.register((a, c, b) -> manager.save());
    }
    
    public Configuration(String name) {
        Path configFolder = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + "/");
        try {
            if (!configFolder.toFile().exists()) {
                Files.createDirectories(configFolder);
            }
        } catch (IOException ignored) {}

        configFile = configFolder.resolve(name).toFile();
        if (!configFile.exists()) save();
    }

    public static Instance getInstance() {
        return manager.instance;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(instance, writer);
        } catch (IOException ignored) {}
    }

    public void load() {
        try (FileReader reader = new FileReader(configFile)) {
            Instance loaded = GSON.fromJson(reader, Instance.class);
            if (loaded != null) {
                instance = loaded;

                instance.types_of_backpacks.replaceAll((key, value) -> value.slots > maxSlots
                        ? defaultInstance.types_of_backpacks.getOrDefault(key, new BackpackType(value.name, 9, true))
                        : value);
            }
        } catch (JsonIOException | JsonSyntaxException | IOException ignored) {}
    }

    public static <K, V> LinkedHashMap<K, V> createMap(Map<K, V> map) {
        return new LinkedHashMap<>(map);
    }

    public record BackpackType(String name, int slots, boolean dyeable) {}

    public static class Instance {
        @SerializedName("breaks_with_flow")
        public boolean breaks_with_flow = true;

        @SerializedName("display_back")
        public boolean display_back = true;

        @SerializedName("types_of_backpacks")
        public Map<Integer, BackpackType> types_of_backpacks = createMap(Map.of(
                1, new BackpackType("small", 9, true),
                2, new BackpackType("medium", 18, true),
                3, new BackpackType("large", 27, true)
        ));

        @SerializedName("back_positions")
        public Map<Integer, Vector3f> back_positions = createMap(Map.of(
                1, new Vector3f(0, -0.45f, 0.280f),
                2, new Vector3f(0, -0.65f, 0.280f),
                3, new Vector3f(0, -0.65f, 0.280f)
        ));

        @SerializedName("back_yaw")
        public Map<Integer, Integer> back_yaw = createMap(Map.of(
                1, 180,
                2, 180,
                3, 180
        ));

        @SerializedName("back_pitch_when_sneaking")
        public Map<Integer, Integer> back_pitch_when_sneaking = createMap(Map.of(
                1, -25,
                2, -25,
                3, -25
        ));
    }
}
