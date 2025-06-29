package com.rouesvm.servback.content.block;

import com.rouesvm.servback.content.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.content.registry.item.BackpackItemJsonRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
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
    protected void writeData(WriteView view) {
        view.putInt("size", size);

        if (item != null) {
            view.putString("item", item.toString());
        } else view.putString("item", BackpackItemJsonRegistry.getBackpackBySize(size).toString());
    }

    @Override
    protected void readData(ReadView view) {
        size = view.getInt("size", 9);

        view.getOptionalInt("dye").ifPresent(integer ->
                item = BackpackItemJsonRegistry.getBackpackBySize(integer +
                        BackpackItemJsonRegistry.getOffset(BackpackItemJsonRegistry.getBackpackUpgradeOrder(size)),
                        size
                ));

        if (item == null) {
            if (view.getOptionalString("item").isPresent()) {
                item = Registries.ITEM.get(Identifier.of(view.getOptionalString("item").get()));
            } else {
                item = Registries.ITEM.get(
                        view.getInt("item", Registries.ITEM.getRawId(
                                BackpackItemJsonRegistry.getBackpackBySize(size)
                        )));
            }
        }
    }

    public ItemStack getDefaultStack() {
        ItemStack stack = item != null ? item.getDefaultStack()
                : BackpackItemJsonRegistry.getBackpackBySize(size).getDefaultStack();
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
