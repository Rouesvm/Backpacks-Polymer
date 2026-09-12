package com.rouesvm.servback.content.block;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import com.rouesvm.servback.technical.BackpackUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class BasicBackpackBlockEntity extends BlockEntity {
    private int size = 9;
    private Component customName;

    private Item item;

    public BasicBackpackBlockEntity(BlockEntityType<?> entityType, BlockPos pos, BlockState state) {
        super(entityType, pos, state);
    }

    public BasicBackpackBlockEntity(BlockPos pos, BlockState state) {
        super(BackpackBlockEntityRegistry.BASIC_BACKPACK_BLOCK_ENTITY, pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        view.putInt("size", size);

        if (item != null)
            view.putString("item", item.toString());
        else view.putString("item", BackpackItemJsonRegistry.getBackpackByName("small").toString());
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        size = view.getIntOr("size", 9);

        if (item == null) {
            Optional<String> itemString = view.getString("item");
            item = itemString.map(Identifier::tryParse)
                    .map(BuiltInRegistries.ITEM::getValue)
                    .orElseGet(() -> {
                        int rawId = view.getIntOr("item", BuiltInRegistries.ITEM.getId(BackpackItemJsonRegistry.getBackpackByName("small")));
                        return BuiltInRegistries.ITEM.byId(rawId);
                    });
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        CustomData component = components.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        Optional<String> itemString = component.copyTag().getString("name");
        ContainerItem item = (ContainerItem) itemString.map(Identifier::tryParse)
                .map(BuiltInRegistries.ITEM::getValue)
                .orElseGet(() -> BackpackItemJsonRegistry.getBackpackByName("small"));

        setItem(item);
        setSize(BackpackUtils.getExtendedSlots(components));

        Component customName = components.get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
            setCustomName(customName);
        }

        setChanged();

        if (this.getLevel() != null) {
            ContainerItem.playPlaceSound(this.getLevel(), getBlockPos());
        }
    }

    public ItemStack getDefaultStack() {
        ItemStack stack = item != null ? item.getDefaultInstance()
                : BackpackItemJsonRegistry.getBackpackByName("small").getDefaultInstance();
        if (customName != null) stack.set(DataComponents.CUSTOM_NAME, customName);
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

    public void setCustomName(Component customName) {
        this.customName = customName;
    }
}
