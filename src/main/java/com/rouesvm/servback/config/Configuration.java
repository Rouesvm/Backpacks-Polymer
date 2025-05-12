package com.rouesvm.servback.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
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
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File configFile;
    private Instance instance = new Instance();

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

    public Instance getInstance() {
        return instance;
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

                if (instance.small_backpack_size > 9 * 6) {
                    instance.small_backpack_size = 9;
                }

                if (instance.medium_backpack_size > 9 * 6) {
                    instance.medium_backpack_size = 9 * 2;
                }

                if (instance.large_backpack_size > 9 * 6) {
                    instance.large_backpack_size = 9 * 3;
                }
            }
        } catch (JsonIOException | JsonSyntaxException | IOException ignored) {}
    }

    public static class Instance {
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
                2, new Vector3f(0, -0.65f, -0.280f),
                3, new Vector3f(0, -0.65f, 0.280f)
        );

        @SerializedName("back_yaw")
        public Map<Integer, Integer> back_yaw = Map.of(
                1, 180,
                2, 0,
                3, 180
        );

        @SerializedName("back_pitch_when_sneaking")
        public Map<Integer, Integer> back_pitch_when_sneaking = Map.of(
                1, -25,
                2, 25,
                3, -25
        );
    }
}
