package com.rouesvm.servback.technical.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.rouesvm.servback.ServerBackpacks;
import net.fabricmc.loader.api.FabricLoader;
import org.joml.Vector3f;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.rouesvm.servback.ServerBackpacks.MOD_ID;

public class BackpackItemConfiguration {
    public record BackCosmetic(Vector3f offset, int yaw, int pitch_while_sneaking) {}
    public record BackpackDefinedType(
            String backpack, String upgradeBackpack,
            int slots, boolean dyeable,
            List<String> dyeBlacklist,
            BackCosmetic cosmetic
    ) {}

    public static final BackCosmetic DEFAULT_COSMETIC = new BackCosmetic(new Vector3f(0, -0.45f, 0.280f), 180, -25);

    public static final BackpackDefinedType DEFAULT_SMALL =  new BackpackDefinedType(
            "small", "medium",
            9, true,
            List.of(),
            DEFAULT_COSMETIC
    );
    public static final BackpackDefinedType DEFAULT_MEDIUM =  new BackpackDefinedType(
            "medium", "large",
            18, true,
            List.of(),
            DEFAULT_COSMETIC
    );
    public static final BackpackDefinedType DEFAULT_LARGE =  new BackpackDefinedType(
            "large", "",
            27, true,
            List.of(),
            DEFAULT_COSMETIC
    );

    public static BackpackItemConfiguration manager;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Path configDir;
    public List<BackpackDefinedType> backpackTypes;

    private BackpackItemConfiguration() {
        configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + "/backpacks/");

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
        } catch (IOException ignored) {}
    }

    private void createDefaultFiles() {
        Path small = configDir.resolve("small.json");
        Path medium = configDir.resolve("medium.json");
        Path large = configDir.resolve("large.json");

        try {
            boolean anyFileExists;
            try (Stream<Path> files = Files.list(configDir)) {
                anyFileExists = files.findAny().isPresent();
            }
            if (anyFileExists) return;

            createDefaultFile(small, DEFAULT_SMALL);
            createDefaultFile(medium, DEFAULT_MEDIUM);
            createDefaultFile(large, DEFAULT_LARGE);

            ServerBackpacks.LOGGER.info("Created default backpack configs in {}", configDir);
        } catch (IOException e) {
            ServerBackpacks.LOGGER.error("Failed to check or create default backpack files: {}", e.getMessage());
        }
    }

    private void createDefaultFile(Path path, BackpackDefinedType data) {
        try (FileWriter writer = new FileWriter(path.toFile())) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            ServerBackpacks.LOGGER.error("Failed to create default file {}: {}", path.getFileName(), e.getMessage());
        }
    }

    public void loadAllBackpacks() {
        backpackTypes = new ArrayList<>();

        if (System.getProperty("fabric-api.datagen") != null) {
            ServerBackpacks.LOGGER.info("Using default backpack configs.");
            backpackTypes.add(DEFAULT_SMALL);
            backpackTypes.add(DEFAULT_MEDIUM);
            backpackTypes.add(DEFAULT_LARGE);
            return;
        }

        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Failed to recreate config directory: {}", e.getMessage());
                return;
            }
        }

        try (Stream<Path> files = Files.list(configDir)) {
            files.filter(path -> path.toString().endsWith(".json"))
                    .forEach(path -> {
                        try (FileReader reader = new FileReader(path.toFile())) {
                            JsonElement rawJson = JsonParser.parseReader(reader);

                            if (rawJson.isJsonArray()) {
                                BackpackDefinedType[] loaded = GSON.fromJson(rawJson, BackpackDefinedType[].class);
                                backpackTypes.addAll(Arrays.asList(loaded));
                            } else {
                                BackpackDefinedType loaded = GSON.fromJson(rawJson, BackpackDefinedType.class);
                                backpackTypes.add(loaded);
                            }

                            ServerBackpacks.LOGGER.info("Loaded backpack config: {}", path.getFileName());
                        } catch (Exception e) {
                            ServerBackpacks.LOGGER.warn("Failed to load backpack file {}: {}", path.getFileName(), e.getMessage());
                        }
                    });
        } catch (IOException e) {
            ServerBackpacks.LOGGER.error("Error reading backpack config directory: {}", e.getMessage());
        }
    }

    public void saveDefinedBackpacks() {
        if (!Files.exists(configDir)) {
            try {
                Files.createDirectories(configDir);
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Failed to create config directory: {}", e.getMessage());
                return;
            }
        }

        for (BackpackDefinedType type : backpackTypes) {
            Path file = configDir.resolve(type.backpack() + ".json");

            try (FileWriter writer = new FileWriter(file.toFile())) {
                GSON.toJson(type, writer);
                ServerBackpacks.LOGGER.info("Saved backpack config: {}", file.getFileName());
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Failed to save backpack {}: {}", type.backpack(), e.getMessage());
            }
        }
    }

    public static void initialize() {
        manager = new BackpackItemConfiguration();

        List<BackpackDefinedType> definedTypes = BackpackConfigurationFixer.convertOldConfigToNew();
        if (definedTypes.isEmpty()) {
            manager.createDefaultFiles();
            manager.loadAllBackpacks();
        } else {
            manager.backpackTypes = definedTypes;
            manager.saveDefinedBackpacks();
        }

        if (manager.backpackTypes.isEmpty()) {
            ServerBackpacks.LOGGER.warn("No valid backpack configs found even after defaults.");
        }

        manager.backpackTypes.forEach(type ->
                ServerBackpacks.LOGGER.debug("Loaded: {} with {} slots.", type.backpack(), type.slots())
        );
    }
}