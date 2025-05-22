package com.rouesvm.servback.config;

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
import java.util.Map;

import static com.rouesvm.servback.Main.MOD_ID;

public class Configuration {
    public static Configuration manager;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File configFile;
    private final Instance defaultInstance = new Instance();
    private Instance instance = new Instance();

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

                instance.small_backpack_size = instance.small_backpack_size > 9 * 6
                        ? defaultInstance.small_backpack_size : instance.small_backpack_size;
                instance.medium_backpack_size = instance.medium_backpack_size > 9 * 6
                        ? defaultInstance.medium_backpack_size : instance.medium_backpack_size;
                instance.large_backpack_size = instance.large_backpack_size > 9 * 6
                        ? defaultInstance.large_backpack_size : instance.large_backpack_size;
            }
        } catch (JsonIOException | JsonSyntaxException | IOException ignored) {}
    }

    public static class Instance {
        @SerializedName("display_back")
        public boolean breaks_with_flow = false;

        @SerializedName("small_backpack_size")
        public int small_backpack_size = 9;

        @SerializedName("medium_backpack_size")
        public int medium_backpack_size = 9 * 2;

        @SerializedName("large_backpack_size")
        public int large_backpack_size = 9 * 3;

        @SerializedName("display_back")
        public boolean display_back = true;

        @SerializedName("back_positions")
        public Map<Integer, Vector3f> back_positions = Map.of(
                1, new Vector3f(0, -0.45f, 0.280f),
                2, new Vector3f(0, -0.65f, 0.280f),
                3, new Vector3f(0, -0.65f, 0.280f)
        );

        @SerializedName("back_yaw")
        public Map<Integer, Integer> back_yaw = Map.of(
                1, 180,
                2, 180,
                3, 180
        );

        @SerializedName("back_pitch_when_sneaking")
        public Map<Integer, Integer> back_pitch_when_sneaking = Map.of(
                1, -25,
                2, -25,
                3, -25
        );
    }
}
