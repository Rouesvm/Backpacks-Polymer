package com.rouesvm.servback.content.block;

import com.rouesvm.servback.content.item.ContainerItem;
import com.rouesvm.servback.content.registry.BackpackItemRegistry;
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
        nbt.putInt("dye", BackpackItemRegistry.getBackpackDyeColor((ContainerItem) item).getIndex());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);

        size = nbt.getInt("size", 9);

        nbt.getInt("dye").ifPresent(integer ->
                item = ContainerItem.getColoredBackpack(DyeColor.byIndex(integer), size / 9));

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

    public void setSize(int size) {
        this.size = size;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setCustomName(Text customName) {
        this.customName = customName;
    }
}
