package com.rouesvm.servback.content.block.impl;

import com.rouesvm.servback.compat.geyser.bedrock.BedrockBlock;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.DyeColor;

public class BackpackBlock extends BaseBackpackBlock implements BlockEntityProvider, BlockWithElementHolder, BedrockBlock {
    public static final BooleanProperty HAS_DYE = BooleanProperty.of("has_dye");
    public static final EnumProperty<DyeColor> DYE_COLOR = EnumProperty.of("dye_color", DyeColor.class);
    public static final IntProperty SLOTS = IntProperty.of("slots", 1, 3);

    public BackpackBlock() {
        super("backpack");
        this.setDefaultState(this.getStateManager().getDefaultState()
                .with(HAS_DYE, false)
                .with(DYE_COLOR, DyeColor.BROWN)
                .with(SLOTS, 1)
        );
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        ContainerItem item = (ContainerItem) context.getStack().getItem();
        DyeColor color = BackpackItemJsonRegistry.getBackpackDyeColor(item);

        int size = item.getSize() / 9;
        if (size > 3) size = 3;

        return super.getPlacementState(context)
                .with(DYE_COLOR, color != null ? color : DyeColor.BROWN)
                .with(HAS_DYE, color != null)
                .with(SLOTS, size);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(HAS_DYE).add(DYE_COLOR).add(SLOTS);
    }
}
