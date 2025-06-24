package com.rouesvm.servback.content.block;

import com.rouesvm.servback.content.item.ContainerItem;
import com.rouesvm.servback.content.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.content.registry.item.BackpackItemRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class BasicBackpackBlockEntity extends BlockEntity {
    private int size = 9;
    private Text customName;

    private Item item;

    public BasicBackpackBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }

    public BasicBackpackBlockEntity(BlockPos pos, BlockState state) {
        super(BackpackBlockEntityRegistry.BASIC_BACKPACK_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.putInt("size", size);

        if (item != null) {
            if (item instanceof ContainerItem containerItem)
                nbt.putInt("dye", BackpackItemRegistry.getBackpackDyeColor(containerItem).getId());
            else nbt.putString("item", item.toString());
        } else nbt.putString("item", BackpackItemRegistry.getBackpack(DyeColor.BROWN, size / 9).toString());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        size = nbt.getInt("size");

        if (nbt.contains("dye")) item = ContainerItem.getColoredBackpack(DyeColor.byId(nbt.getInt("dye")), size / 9);

        if (item == null) {
            if (nbt.contains("item"))
                item = Registries.ITEM.get(Identifier.of(nbt.getString("item")));
            else item = Registries.ITEM.get(Registries.ITEM.getRawId(
                    ContainerItem.getDefaultBackpack(1)));
        }
    }

    public ItemStack getDefaultStack() {
        ItemStack stack = item != null ? item.getDefaultStack() : ContainerItem.getDefaultBackpack(size / 9).getDefaultStack();
        if (customName != null) stack.set(DataComponentTypes.CUSTOM_NAME, customName);
        return stack;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setCustomName(Text customName) {
        this.customName = customName;
    }
}
