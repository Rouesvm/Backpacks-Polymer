package com.rouesvm.servback.item;

import com.rouesvm.servback.block.BackpackBlockEntity;
import com.rouesvm.servback.registry.BackpackBlockRegistry;
import com.rouesvm.servback.registry.BackpackItemRegistry;
import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import com.rouesvm.servback.utils.BackpackInstance;
import com.rouesvm.servback.utils.BackpackManager;
import com.rouesvm.servback.utils.BackpackUtils;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ContainerItem extends BasicPolymerBlockItem {
    public final int slots;
    private final DyeColor color;

    public ContainerItem(String name, int slots, DyeColor color) {
        super(name, Items.LEATHER, BackpackBlockRegistry.BACKPACK);
        this.slots = slots;
        this.color = color;
    }

    @Override
    public boolean canBeNested() {
        return false;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
        BackpackInventory itemList = BackpackUtils.getItemList(stack, this.slots);

        if (itemList == null) return;
        if (itemList.getHeldStacks().isEmpty()) return;

        int capacityMaxShow = 0;
        int capacityAmount = 0;

        for (ItemStack itemStack : itemList.getHeldStacks()) {
            if (itemStack.isEmpty()) continue;

            capacityAmount++;

            if (capacityMaxShow <= 4) {
                capacityMaxShow++;
                tooltip.add(Text.translatable("container.shulkerBox.itemCount", itemStack.getName(), itemStack.getCount()).formatted(Formatting.GOLD));
            }
        }

        if (capacityAmount - capacityMaxShow > 0) {
            tooltip.add(Text.translatable("container.shulkerBox.more", capacityAmount - capacityMaxShow).formatted(Formatting.ITALIC).formatted(Formatting.GOLD));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        var cast = player.raycast(5,0,false);
        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return TypedActionResult.pass(stack);
        if (player.isSneaking())
            return TypedActionResult.pass(stack);
        if (cast.getType() == HitResult.Type.BLOCK)
            return TypedActionResult.pass(stack);

        openGui(serverPlayer, stack);
        player.swingHand(hand, true);
        return TypedActionResult.success(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getPlayer() instanceof ServerPlayerEntity serverPlayer))
            return ActionResult.PASS;
        if (serverPlayer.isSneaking())
            return super.useOnBlock(context);

        openGui(serverPlayer, context.getStack());
        serverPlayer.swingHand(context.getHand(), true);
        return ActionResult.SUCCESS;
    }

    public ActionResult place(ItemPlacementContext context) {
        if (!this.getBlock().isEnabled(context.getWorld().getEnabledFeatures())) {
            return ActionResult.FAIL;
        } else if (!context.canPlace()) {
            return ActionResult.FAIL;
        } else {
            ItemPlacementContext itemPlacementContext = this.getPlacementContext(context);
            if (itemPlacementContext == null) {
                return ActionResult.FAIL;
            } else {
                BlockState blockState = this.getPlacementState(itemPlacementContext);
                if (blockState == null) {
                    return ActionResult.FAIL;
                } else if (!this.place(itemPlacementContext, blockState)) {
                    return ActionResult.FAIL;
                } else {
                    BlockPos blockPos = itemPlacementContext.getBlockPos();
                    World world = itemPlacementContext.getWorld();
                    PlayerEntity playerEntity = itemPlacementContext.getPlayer();
                    ItemStack itemStack = itemPlacementContext.getStack();
                    BlockState blockState2 = world.getBlockState(blockPos);

                    if (blockState2.isOf(blockState.getBlock())) {
                        this.postPlacement(blockPos, world, playerEntity, itemStack, blockState2);
                        blockState2.getBlock().onPlaced(world, blockPos, blockState2, playerEntity, itemStack);

                        if (world.getBlockEntity(blockPos) instanceof BackpackBlockEntity blockEntity) {
                            blockEntity.setUuid(BackpackManager.getStackUUID(context.getStack()));
                            blockEntity.setExtraSize(BackpackUtils.getExtendedSlots(context.getStack()));
                            blockEntity.setSize(slots);
                            blockEntity.setColor(color);

                            if (itemStack.getName() != null) {
                                blockEntity.setCustomName(itemStack.getName());
                            }
                        }

                        if (playerEntity instanceof ServerPlayerEntity) {
                            Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)playerEntity, blockPos, itemStack);
                        }
                    }

                    if (playerEntity != null)
                        playerEntity.playSoundToPlayer(SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS,
                                1,
                                0.5F * context.getWorld().getRandom().nextFloat() * 0.8F);

                    world.emitGameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Emitter.of(playerEntity, blockState2));
                    itemStack.decrementUnlessCreative(1, playerEntity);
                    return ActionResult.SUCCESS;
                }
            }
        }
    }

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

    public DyeColor getColor() {
        return color;
    }

    // It's 1-9 (If you have slots = (9 * (size)) you do (slots / 9))
    public static Item getColoredBackpack(DyeColor color, int size) {
        return color != null ? BackpackItemRegistry.getBackpack(color, size) : getDefaultBackpack(size);
    }

    public static Item getDefaultBackpack(int size) {
        Item item = BackpackItemRegistry.getBackpack(DyeColor.BROWN, size);
        return item != null ? item : BackpackItemRegistry.getBackpack(DyeColor.BROWN, 1);
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
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

}

