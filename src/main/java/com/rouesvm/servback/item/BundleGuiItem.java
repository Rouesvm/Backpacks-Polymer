package com.rouesvm.servback.item;

import com.rouesvm.servback.block.BasicBlockEntity;
import com.rouesvm.servback.ui.BasicGui;
import com.rouesvm.servback.ui.inventory.BaseInventory;
import com.rouesvm.servback.utils.BackpackUtils;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import static net.minecraft.item.BundleItem.setSelectedStackIndex;

public class BundleGuiItem extends BasicPolymerBlockItem  {
    public BundleGuiItem(String name, Block block) {
        super(name, Items.LEATHER, block);
    }

    @Override
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

                        if (world.getBlockEntity(blockPos) instanceof BasicBlockEntity blockEntity) {
                            blockEntity.setItem(this);
                            blockEntity.setSize(BackpackUtils.getExtendedSlots(context.getStack()));

                            if (itemStack.getCustomName() != null) {
                                blockEntity.setCustomName(itemStack.getCustomName());
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

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        var cast = player.raycast(5,0,false);
        if (!(player instanceof ServerPlayerEntity serverPlayer))
            return ActionResult.PASS;
        if (player.isSneaking())
            return ActionResult.PASS;
        if (cast.getType() == HitResult.Type.BLOCK)
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;

        openGui(serverPlayer, stack);
        player.swingHand(hand, true);
        return ActionResult.SUCCESS;
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

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        Inventory inventory = getInventory(serverPlayer, stack);

        if (inventory == null) {
            return false;
        } else {
            ItemStack itemStack = slot.getStack();

            if (!itemStack.getItem().canBeNested()) return false;

            if (clickType == ClickType.LEFT && !itemStack.isEmpty()) {
                if (BaseInventory.canInsert(itemStack, inventory)) {
                    itemStack = BaseInventory.addStack(itemStack, inventory);
                    ContainerItem.playInsertSound(serverPlayer);
                } else {
                    ContainerItem.playInsertFailSound(serverPlayer);
                }

                slot.setStack(itemStack);
                afterChanged(serverPlayer, stack, inventory);
                onContentChanged(player);
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.LEFT && otherStack.isEmpty()) {
            setSelectedStackIndex(stack, -1);
        } else {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            Inventory inventory = getInventory(serverPlayer, stack);

            if (!otherStack.getItem().canBeNested()) return false;

            if (inventory != null) {
                if (clickType == ClickType.LEFT && !otherStack.isEmpty()) {
                    if (BaseInventory.canInsert(otherStack, inventory)) {
                        otherStack = BaseInventory.addStack(otherStack, inventory);
                        ContainerItem.playInsertSound(serverPlayer);
                    } else {
                        ContainerItem.playInsertFailSound(serverPlayer);
                    }

                    cursorStackReference.set(otherStack);
                    afterChanged(serverPlayer, stack, inventory);
                    onContentChanged(player);
                    return true;
                } else if (clickType == ClickType.RIGHT) {
                    openGui(serverPlayer, stack);
                    return true;
                } else {
                    setSelectedStackIndex(stack, -1);
                }
            }
        }
        return false;
    }

    public Inventory getInventory(ServerPlayerEntity player, ItemStack stack) {
        return null;
    }

    public void afterChanged(ServerPlayerEntity player, ItemStack stack, Inventory inventory) {
    }

    public void onContentChanged(PlayerEntity user) {
        ScreenHandler screenHandler = user.currentScreenHandler;
        if (screenHandler != null) {
            screenHandler.onContentChanged(user.getInventory());
        }
    }

    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        ContainerItem.playInsertSound(player);
        new BasicGui(player, stack, getInventory(player, stack));
    }
}
