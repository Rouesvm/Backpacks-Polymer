package com.rouesvm.servback.technical.manager;

import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BackpackUUID {
    public static @Nullable UUID getStackUUID(ItemStack stack) {
        UUID uuid = stack.get(BackpackDataComponentTypes.BACKPACK_UUID);

        if (uuid == null) {
            String legacy = stack.get(BackpackDataComponentTypes.STRING_UUID);
            if (legacy != null) {
                uuid = UUID.fromString(legacy);
                stack.set(BackpackDataComponentTypes.BACKPACK_UUID, uuid);
                stack.remove(BackpackDataComponentTypes.STRING_UUID);
            }
        }

        return uuid;
    }

    public static UUID createNewUUID(ItemStack stack) {
        UUID uuid = getStackUUID(stack);
        if (uuid == null) {
            uuid = generateUniqueUUID();
            stack.set(BackpackDataComponentTypes.BACKPACK_UUID, uuid);
        }
        return uuid;
    }

    public static UUID generateUniqueUUID() {
        UUID uuid = UUID.randomUUID();
        if (BackpackManager.instance() != null && BackpackManager.instance().hasBackpack(uuid)) {
            uuid = UUID.randomUUID();
        }
        return uuid;
    }
}
