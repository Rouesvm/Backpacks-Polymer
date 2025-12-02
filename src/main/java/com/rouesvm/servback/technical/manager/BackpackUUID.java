package com.rouesvm.servback.technical.manager;

import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BackpackUUID {
    public static @NotNull UUID getUUIDOrCreateNew(ItemStack stack) {
        UUID uuid = getStackUUID(stack);
        return uuid != null ? uuid : createNewUUID(stack);
    }

    public static @Nullable UUID getStackUUID(ItemStack stack) {
        if (stack == null) return null;

        UUID uuid = stack.get(BackpackDataComponentTypes.BACKPACK_UUID);

        if (uuid == null) {
            String legacy = stack.get(BackpackDataComponentTypes.STRING_UUID);
            if (legacy != null) {
                uuid = UUID.fromString(legacy);
                stack.set(BackpackDataComponentTypes.BACKPACK_UUID, uuid);
                stack.remove(BackpackDataComponentTypes.STRING_UUID);
            }
        }

        if (uuid != null) BackpackManager.addUUIDIfEmpty(uuid);

        return uuid;
    }

    public static UUID createNewUUID(ItemStack stack) {
        UUID uuid = generateUniqueUUID();
        stack.set(BackpackDataComponentTypes.BACKPACK_UUID, uuid);
        return uuid;
    }

    public static UUID generateUniqueUUID() {
        UUID uuid = UUID.randomUUID();
        if (BackpackManager.instance() != null && BackpackManager.hasUUID(uuid)) {
            uuid = UUID.randomUUID();
        }
        return uuid;
    }
}
