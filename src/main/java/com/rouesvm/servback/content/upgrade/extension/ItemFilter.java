package com.rouesvm.servback.content.upgrade.extension;

import com.rouesvm.servback.content.upgrade.PersistentUpgrade;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.Set;

public class ItemFilter implements PersistentUpgrade {
    private MODE mode;
    private final Set<String> filterList;

    public ItemFilter(MODE mode, Set<String> filters) {
        this.mode = mode;
        this.filterList = filters;
    }

    public boolean matches(ItemStack stack) {
        return filterList.stream()
                .anyMatch(filterID -> matchesFilter(filterID, stack.getItem()));
    }

    public boolean matches(ItemStack stack, BackpackInventory inventory) {
        return inventory.containsAny(inventoryStack -> inventoryStack.isOf(stack.getItem()));
    }

    public static boolean matchesFilter(String filterID, Item item) {
        var itemRegistry = Registries.ITEM;
        var itemEntry = itemRegistry.getEntry(item);

        if (filterID.startsWith("#")) {
            Identifier tagId = Identifier.tryParse(filterID.substring(1));
            if (tagId == null) return false;

            TagKey<Item> tag = TagKey.of(itemRegistry.getKey(), tagId);
            return itemEntry.isIn(tag);
        }

        String itemId = itemEntry.getIdAsString();
        return itemId.equals(filterID);
    }

    @Override
    public void readView(NbtCompound data) {
        this.mode = MODE.values()[data.getInt("mode")];

        filterList.clear();

        if (data.contains("Items", NbtElement.LIST_TYPE)) {
            NbtList items = data.getList("Items", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < items.size(); i++) {
                String itemStr = items.getCompound(i).getString("item");
                if (!itemStr.isEmpty()) filterList.add(itemStr);
            }
        }
    }

    @Override
    public void writeView(NbtCompound data) {
        if (this.mode != null) data.putInt("mode", mode.ordinal());

        NbtList items = new NbtList();
        filterList.stream()
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    NbtCompound nbt = new NbtCompound();
                    nbt.putString("item", s);
                    return nbt;
                })
                .forEach(items::add);

        if (!items.isEmpty())
            data.put("Items", items);
        else data.remove("Items");
    }

    public Set<String> filterList() {
        return filterList;
    }

    public MODE getMode() {
        return mode;
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    public enum MODE {
        BLACKLIST,
        WHITELIST,
        MATCH_CONTENT,
        PICKUP
    }
}
