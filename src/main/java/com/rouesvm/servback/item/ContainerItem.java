package com.rouesvm.servback.item;

import com.rouesvm.servback.block.backpack.BackpackBlockEntity;
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
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.UUID;

public class ContainerItem extends BundleGuiItem {
    public final int slots;

    public ContainerItem(String name, int slots) {
        super(name, BackpackBlockRegistry.BACKPACK);
        this.slots = slots;
    }

    public int getSize() {
        return slots / 9;
    }

    @Override
    public boolean canBeNested() {
        return false;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack polymerStack, PacketContext context) {
        DefaultedList<ItemStack> itemList = BackpackUtils.getItemList(polymerStack);

        if (itemList == null) return;
        if (itemList.isEmpty()) return;

        int capacityMaxShow = 0;
        int capacityAmount = 0;

        for (ItemStack itemStack : itemList) {
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
                            BackpackUtils.resizeIfIncorrectSize((ServerPlayerEntity) playerEntity, itemStack, this.slots);

                            blockEntity.setItem(this);
                            blockEntity.setExtraSize(BackpackUtils.getExtendedSlots(context.getStack()));
                            blockEntity.setSize(slots);
                            blockEntity.setUuid(BackpackManager.getStackUUID(context.getStack()));

                            blockEntity.setStorage();

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
    public Inventory getInventory(ServerPlayerEntity player, ItemStack stack) {
        return BackpackManager.getInventory(BackpackManager.getStackUUID(stack));
    }

    @Override
    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        BackpackManager.createNewUUID(stack);
        BackpackUtils.resizeIfIncorrectSize(player, stack, this.slots);
        playInsertSound(player);

        System.out.println("test");

        BackpackInstance instance = BackpackManager.getInstance(BackpackManager.getStackUUID(stack), this.slots + BackpackUtils.getExtendedSlots(stack));
        System.out.println(instance);
        if (instance != null) new BackpackGui(player, stack, instance);
    }

    @Override
    public void afterChanged(ServerPlayerEntity player, ItemStack stack, Inventory inventory) {
        UUID uuid = BackpackManager.getStackUUID(stack);
        BackpackManager.addBackpack(uuid, (BackpackInventory) inventory);
    }

    public DefaultedList<ItemStack> getComponentItemList(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.slots, ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
        return list;
    }

    // It's 1-9 (If you have slots = (9 * (order)) you do (slots / 9))
    public static Item getColoredBackpack(DyeColor color, int order) {
        return color != null ? BackpackItemRegistry.getBackpack(color, order) : getDefaultBackpack(order);
    }

    public static Item getDefaultBackpack(int order) {
        Item item = BackpackItemRegistry.getBackpack(DyeColor.BROWN, order);
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
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

}

