package com.rouesvm.servback.blocks;

import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.registry.BackpackBlockEntityRegistry;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class BackpackBlockEntity extends BlockEntity {
    private UUID uuid;
    private int size = 9;
    private int extraSize = 0;

    private DyeColor color;

    public BackpackBlockEntity(BlockPos pos, BlockState state) {
        super(BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt("dye", color.getIndex());
        nbt.putInt("size", size);
        nbt.putInt("extraSize", extraSize);
        nbt.putString("uuid", uuid.toString());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        color = DyeColor.byIndex(nbt.getInt("dye", 12));
        size = nbt.getInt("size", 9);
        extraSize = nbt.getInt("extraSize", 0);
        uuid = UUID.fromString(nbt.getString("uuid", UUID.randomUUID().toString()));
    }

    public ItemStack getItemStack() {
        ItemStack stack = ContainerItem.getColoredBackpack(color, size).getDefaultStack();
        stack.set(BackpackDataComponentTypes.UUID_TYPE, uuid.toString());
        return stack;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getExtraSize() {
        return extraSize;
    }

    public void setExtraSize(int extraSize) {
        this.extraSize = extraSize;
    }

    public DyeColor getColor() {
        return color;
    }

    public void setColor(DyeColor color) {
        this.color = color;
    }
}
