package com.rouesvm.servback.technical.data.sql;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.data.BackpackData;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.state.codecs.BackpackInstanceData;
import com.rouesvm.servback.technical.data.state.codecs.InventoryData;
import com.rouesvm.servback.technical.data.state.codecs.SlotData;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;

public class BackpackSQL {
    private Connection connection;
    private Statement statement;

    private final MinecraftServer server;

    public BackpackSQL(MinecraftServer server) {
        this.server = server;
    }

    public void close() {
        try {
            if (statement != null && !statement.isClosed()) statement.close();
            if (connection != null && !connection.isClosed()) connection.close();
        } catch (SQLException e) {
            ServerBackpacks.LOGGER.error("Failed to close SQL connection", e);
        }
    }

    public String serializeBackpackData(BackpackInstanceData data) {
        DataResult<JsonElement> result = BackpackInstanceData.CODEC.encodeStart(server.getRegistryManager().getOps(JsonOps.INSTANCE), data);
        Optional<JsonElement> jsonOptional = result.result();
        if (jsonOptional.isPresent()) {
            return jsonOptional.map(jsonElement -> new GsonBuilder().create().toJson(jsonElement)).orElseThrow();
        } else return "";
    }

    public BackpackInstanceData deserializeBackpackData(String json) {
        var jsonElement = new Gson().fromJson(json, com.google.gson.JsonElement.class);
        DataResult<BackpackInstanceData> result = BackpackInstanceData.CODEC.parse(server.getRegistryManager().getOps(JsonOps.INSTANCE), jsonElement);
        return result.result().orElseThrow();
    }

    public boolean saveInventories() {
        List<BackpackInstanceData> backpackInstanceData = BackpackData.getStoredInventories();
        String sql = "REPLACE INTO backpacks (uuid, backpack_data) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (BackpackInstanceData data : backpackInstanceData) {
                String inventory = serializeBackpackData(data);
                ps.setString(1, data.uuid().toString());
                ps.setString(2, inventory);
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
            return true;
        } catch (SQLException e) {
            ServerBackpacks.LOGGER.error("Failed to save backpack inventories", e);
            return false;
        }
    }

    public Set<BackpackInstance> loadInventories() {
        Set<BackpackInstance> inventories = new HashSet<>();
        String sql = "SELECT uuid, backpack_data FROM backpacks";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String jsonString = rs.getString("backpack_data");
                BackpackInstanceData data = deserializeBackpackData(jsonString);

                inventories.add(new BackpackInstance(uuid, new BackpackInventory(InventoryData.getHeldStacks(data.getInventoryData().itemStacks()))));
            }
        } catch (Exception e) {
            ServerBackpacks.LOGGER.error("Failed to load backpack inventories", e);
        }
        return inventories;
    }

    public boolean saveInventory(BackpackInstance instance) {
        if (instance == null || instance.getUuid() == null) return false;

        String sql = "REPLACE INTO backpacks (uuid, backpack_data) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, instance.getUuid().toString());

            BackpackInstanceData data = new BackpackInstanceData(instance.getUuid(), new InventoryData(SlotData.writeToCodec(instance.heldInventory())));
            String jsonString = serializeBackpackData(data);

            ps.setString(2, jsonString);
            ps.executeUpdate();
            connection.commit();
            return true;
        } catch (Exception e) {
            ServerBackpacks.LOGGER.error("Failed to save backpack inventory for UUID: {}", instance.getUuid(), e);
            return false;
        }
    }

    public Optional<BackpackInstance> loadInventory(UUID uuid) {
        String sql = "SELECT uuid, backpack_data FROM backpacks WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String jsonString = rs.getString("backpack_data");
                    BackpackInstanceData data = deserializeBackpackData(jsonString);
                    BackpackInventory inventory = new BackpackInventory(InventoryData.getHeldStacks(data.getInventoryData().itemStacks()));
                    return Optional.of(new BackpackInstance(uuid, inventory));
                }
            }
        } catch (Exception e) {
            ServerBackpacks.LOGGER.error("Failed to load backpack inventory for UUID: {}", uuid, e);
        }
        return Optional.empty();
    }

    public boolean createConnection() {
        boolean connected;

        if (Configuration.instance().sql_host == null || Configuration.instance().sql_host.isEmpty()) {
            connected = createSQLiteConnection();
        } else connected = createMySQLConnection();

        if (connection != null) {
            ServerBackpacks.LOGGER.info("Connected to SQL database!");
            try {
                statement = connection.createStatement();
            } catch (SQLException e) {
                ServerBackpacks.LOGGER.error("Failed to create statement!", e);
                return false;
            }
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                ServerBackpacks.LOGGER.error("Failed to set auto commit!", e);
                return false;
            }
        }
        return connected && connection != null && statement != null;
    }

    public void createTableIfNotExists() {
        try {
            try (Statement stmt = connection.createStatement()) {
                String sql = """
                    CREATE TABLE IF NOT EXISTS backpacks (
                        uuid VARCHAR(36) PRIMARY KEY,
                        backpack_data TEXT NOT NULL
                     );
                    """;
                stmt.executeUpdate(sql);
                connection.commit();
            }
        } catch (SQLException e) {
            ServerBackpacks.LOGGER.error("Failed to create table!", e);
        }
    }

    public boolean createMySQLConnection() {
        String url = String.format(
                "jdbc:mysql://%s:%d/%s?connectTimeout=%d&autoReconnect=true&useSSL=false",
                Configuration.instance().sql_host,
                Configuration.instance().sql_port,
                Configuration.instance().sql_database,
                Configuration.instance().sql_timeout * 1000 // milliseconds
        );

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            ServerBackpacks.LOGGER.error("Failed to load MySQL driver", e);
            return false;
        }

        try {
            connection = DriverManager.getConnection(
                    url,
                    Configuration.instance().sql_username,
                    Configuration.instance().sql_password
            );
        } catch (SQLException e) {
            ServerBackpacks.LOGGER.error("Failed to connect to MySQL database", e);
            return false;
        }

        return connection != null;
    }

    public boolean createSQLiteConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            ServerBackpacks.LOGGER.error("Failed to load SQLite driver", e);
            return false;
        }

        Path path = Configuration.manager.getConfigDir();
        if (!path.toFile().exists()) path.toFile().mkdirs();

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:database.db");
        } catch (SQLException e) {
            ServerBackpacks.LOGGER.error("Failed to connect to SQLite database", e);
            return false;
        }

        return connection != null;
    }
}
