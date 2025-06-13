package com.rouesvm.servback.content.block.backpack;

import com.rouesvm.servback.compat.geyser.bedrock.BedrockBlock;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.content.block.BasicBackpackBlock;
import com.rouesvm.servback.content.block.BasicBlockEntity;
import com.rouesvm.servback.content.item.ContainerItem;
import com.rouesvm.servback.content.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.content.registry.item.BackpackItemRegistry;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.BackpackManager;
import com.rouesvm.servback.technical.data.BackpackUtils;
import com.rouesvm.servback.technical.ui.BackpackGui;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.rouesvm.servback.technical.data.BackpackUtils.resize;

public class BackpackBlock extends BasicBackpackBlock implements BlockEntityProvider, BlockWithElementHolder, BedrockBlock {
    public static final EnumProperty<DyeColor> DYE_COLOR = EnumProperty.of("dye_color", DyeColor.class);
    public static final IntProperty SLOTS = IntProperty.of("slots", 1, 3);

    public BackpackBlock() {
        super("backpack");
        this.setDefaultState(super.stateManager.getDefaultState().with(DYE_COLOR, DyeColor.BROWN).with(SLOTS, 1));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        ContainerItem item = (ContainerItem) context.getStack().getItem();
        return super.getPlacementState(context)
                .with(DYE_COLOR, BackpackItemRegistry.getBackpackDyeColor(item))
                .with(SLOTS, item.getSize());
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING).add(DYE_COLOR).add(SLOTS);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(getInventory(
                null, world.getBlockEntity(pos, BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY).get()
        ));
    }

    @Override
    public boolean trinketInteraction(BasicBlockEntity entity, ServerPlayerEntity player, World world, BlockPos pos) {
        if (BackpackTrinket.isStackEmptyInBackSlot(player)) {
            BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) entity;

            ItemStack stack = backpackBlockEntity.getDefaultStack().copy();
            BackpackUtils.resizeIfIncorrectSize(player, stack, backpackBlockEntity.getSize());
            BackpackTrinket.equipStack(player, stack);
            world.breakBlock(pos, false);
            return true;
        }
        return false;
    }

    @Override
    public void openGui(ServerPlayerEntity player, BlockEntity entity) {
        if (entity instanceof BackpackBlockEntity backpackBlockEntity) {
            BackpackInstance instance = backpackBlockEntity.getInstance();
            resize(player, backpackBlockEntity.getUuid(), instance.inventory(),
                    backpackBlockEntity.getSize() + backpackBlockEntity.getExtraSize());
            new BackpackGui(player, instance);
        }
    }

    @Override
    public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable BlockEntity entity) {
        if (entity == null) return null;
        BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) entity;
        return BackpackManager.getInventory(backpackBlockEntity.getUuid());
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY.instantiate(pos, state);
    }
}
