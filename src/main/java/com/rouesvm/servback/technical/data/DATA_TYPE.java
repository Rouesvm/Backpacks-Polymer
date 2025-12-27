package com.rouesvm.servback.technical.data;

public enum DATA_TYPE {
    NONE("No data loaded"),
    MINECRAFT_STATE("Minecraft state"),
    OLD_MINECRAFT_STATE("Old Minecraft state"),
    FILE_DATA("File-based data"),
    LIST_FILE_DATA("List file-based data");

    private final String description;

    DATA_TYPE(String desc) { this.description = desc; }

    @Override
    public String toString() { return description; }
}
