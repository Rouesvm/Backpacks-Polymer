package com.rouesvm.servback.items;

import com.rouesvm.servback.blocks.BackpackBlockEntity;
import com.rouesvm.servback.registry.BackpackBlockRegistry;
import com.rouesvm.servback.registry.BackpackItemRegistry;
import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import com.rouesvm.servback.utils.BackpackInstance;
import com.rouesvm.servback.utils.BackpackManager;
import com.rouesvm.servback.utils.BackpackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class ContainerItem extends GuiItem {
    public final int slots;
    private final DyeColor color;

    public ContainerItem(String name, int slots, DyeColor color) {
        super(name);
        this.slots = slots;
        this.color = color;
    }

    @Override
    public boolean canBeNested() {
        return false;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack polymerStack, PacketContext context) {
        BackpackInventory itemList = BackpackUtils.getItemList(polymerStack, this.slots);

        if (itemList == null) return;
        if (itemList.getHeldStacks().isEmpty()) return;

        int capacityMaxShow = 0;
        int capacityAmount = 0;

        for (ItemStack itemStack : itemList.getHeldStacks()) {
            if (itemStack.isEmpty()) continue;

            capacityAmount++;

            if (capacityMaxShow <= 4) {
                capacityMaxShow++;
                tooltip.add(Text.translatable("item.container.item_count", itemStack.getName(), itemStack.getCount()).formatted(Formatting.GOLD));
            }
        }

        if (capacityAmount - capacityMaxShow > 0) {
            tooltip.add(Text.translatable("item.container.more_items", capacityAmount - capacityMaxShow).formatted(Formatting.ITALIC).formatted(Formatting.GOLD));
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() instanceof ServerPlayerEntity player && player.isSneaking()) {
            BlockState state = BackpackBlockRegistry.BACKPACK.getDefaultState();
            ServerWorld world = (ServerWorld) context.getWorld();

            BlockPos pos = context.getBlockPos();

            state = state.with(HorizontalFacingBlock.FACING, player.getHorizontalFacing());

            if (world.getBlockState(context.getBlockPos()).isIn(BlockTags.REPLACEABLE) && world.canPlace(state, context.getBlockPos(), ShapeContext.ofPlacement(player))) {
                world.setBlockState(pos, state);
            } else if (world.canPlace(state, context.getBlockPos().up(), ShapeContext.ofPlacement(player))) {
                pos = pos.up();
                world.setBlockState(pos, state);
            }

            BackpackBlockEntity entity = (BackpackBlockEntity) world.getBlockEntity(pos);
            if (entity != null) {
                entity.setUuid(BackpackManager.getStackUUID(context.getStack()));
                entity.setExtraSize(BackpackUtils.getExtendedSlots(context.getStack()));
                entity.setSize(slots);
                entity.setColor(color);
                entity.createVisual(state, pos, world);
            }

            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    @Override
    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        onOpen(player, stack);

        BackpackInstance instance = BackpackManager.getInstance(
                BackpackManager.getStackUUID(stack),
                BackpackUtils.getExtendedSlots(stack) + this.slots
        );

        new BackpackGui(player, stack, instance);
    }

    public DefaultedList<ItemStack> getComponentItemList(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.slots + BackpackUtils.getExtendedSlots(stack), ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
        return list;
    }

    public void onOpen(ServerPlayerEntity player, ItemStack stack) {
        BackpackManager.createNewUUID(stack);
        BackpackUtils.checkEnchantments(stack, player, this.slots);
        playInsertSound(player);
    }

    public int getSize() {
        return slots / 9;
    }

    public static Item getBackpackMap(DyeColor color, int size) {
       switch (size) {
           case 1 -> {
               return BackpackItemRegistry.SMALL.get(color);
           } case 2 -> {
               return BackpackItemRegistry.MEDIUM.get(color);
           } case 3 -> {
               return BackpackItemRegistry.LARGE.get(color);
           }
       }

       return getDefaultBackpack(size);
    }

    public static Item getColoredBackpack(DyeColor color, int size) {
        Item item;
        if (color != null)
            item = getBackpackMap(color, size);
        else item = getDefaultBackpack(size);
        return item;
    }

    public static Item getDefaultBackpack(int size) {
        return switch (size) {
            case 1 -> BackpackItemRegistry.SMALL.get(DyeColor.BROWN);
            case 2 -> BackpackItemRegistry.MEDIUM.get(DyeColor.BROWN);
            default -> BackpackItemRegistry.LARGE.get(DyeColor.BROWN);
        };
    }

    public static void playInsertSound(ServerPlayerEntity player) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public static void playDropContentsSound(ServerPlayerEntity player) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public static void playRemoveOneSound(ServerPlayerEntity player) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public static void playInsertFailSound(ServerPlayerEntity player) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

}

