package com.rouesvm.servback.content.block.impl;

import com.rouesvm.servback.compat.geyser.bedrock.BedrockBlock;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.datagen.ModItemTags;
import com.rouesvm.servback.registry.item.BackpackItemJsonRegistry;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class BackpackBlock extends BaseBackpackBlock implements EntityBlock, BlockWithElementHolder, BedrockBlock {
    public static final BooleanProperty HAS_DYE = BooleanProperty.create("has_dye");
    public static final EnumProperty<DyeColor> DYE_COLOR = EnumProperty.create("dye_color", DyeColor.class);
    public static final IntegerProperty SLOTS = IntegerProperty.create("slots", 1, 3);

    public BackpackBlock() {
        super("backpack");
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(HAS_DYE, false)
                .setValue(DYE_COLOR, DyeColor.BROWN)
                .setValue(SLOTS, 1)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        ItemStack itemStack = context.getItemInHand();
        ContainerItem item = (ContainerItem) itemStack.getItem();
        DyeColor color = BackpackItemJsonRegistry.getBackpackDyeColor(item);

        int slot = 1;
        if (itemStack.is(ModItemTags.MEDIUM_BACKPACKS)) {
            slot = 2;
        } else if (itemStack.is(ModItemTags.LARGE_BACKPACKS)) {
            slot = 3;
        }

        return super.getStateForPlacement(context)
                .setValue(DYE_COLOR, color != null ? color : DyeColor.BROWN)
                .setValue(HAS_DYE, color != null)
                .setValue(SLOTS, slot);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(HAS_DYE).add(DYE_COLOR).add(SLOTS);
    }
}
