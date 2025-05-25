package com.rouesvm.servback.block;

import com.rouesvm.servback.item.ContainerItem;
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
import net.minecraft.util.math.BlockPos;

public class BasicBlockEntity extends BlockEntity {
    private int size = 9;
    private Text customName;

    private Item item;

    public BasicBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);

        nbt.putInt("size", size);
        if (item != null) nbt.putInt("item", Registries.ITEM.getRawId(item));
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        size = nbt.getInt("size", 9);

        nbt.getInt("dye").ifPresent(integer -> {
            item = ContainerItem.getColoredBackpack(DyeColor.byIndex(integer), size / 9);
            nbt.putInt("dye", -1);
        });

        if (item == null) item = Registries.ITEM.get(
                nbt.getInt("item", Registries.ITEM.getRawId(
                        ContainerItem.getDefaultBackpack(1)
                )));
    }

    public ItemStack getDefaultStack() {
        ItemStack stack = item != null ? item.getDefaultStack() : ContainerItem.getDefaultBackpack(1).getDefaultStack();
        if (customName != null) stack.set(DataComponentTypes.CUSTOM_NAME, customName);
        return stack;
    }

    public int getSize() {
        return size;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setCustomName(Text customName) {
        this.customName = customName;
    }
}
