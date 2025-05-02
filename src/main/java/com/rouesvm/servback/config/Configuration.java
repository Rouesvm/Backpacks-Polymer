package com.rouesvm.servback.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        @SerializedName("//comment_1")
        public String comment_1 = "Cannot be more than 54 slots";

        @SerializedName("small_backpack_size")
        public int small_backpack_size = 9;

        @SerializedName("medium_backpack_size")
        public int medium_backpack_size = 9 * 2;

        @SerializedName("large_backpack_size")
        public int large_backpack_size = 9 * 3;

        @SerializedName("//comment_2")
        public String comment_2 = "If enabled it will display the backpack on the back if you equipped it on the trinket back slot";

        @SerializedName("display_back")
        public boolean display_back = true;
    }
}
