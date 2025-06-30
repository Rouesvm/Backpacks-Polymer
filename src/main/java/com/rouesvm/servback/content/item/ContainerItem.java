package com.rouesvm.servback.content.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.block.backpack.BackpackBlockEntity;
import com.rouesvm.servback.content.registry.block.BackpackBlockRegistry;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.BackpackManager;
import com.rouesvm.servback.technical.data.BackpackUtils;
import com.rouesvm.servback.technical.ui.BackpackGui;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ContainerItem extends BundleGuiItem {
    public final int slots;

    public ContainerItem(String name, int slots) {
        super(name, BackpackBlockRegistry.BACKPACK);
        this.slots = slots;
    }

    public int getSize() {
        return slots;
    }

    @Override
    public boolean canBeNested() {
        return false;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack polymerStack, PacketContext context) {
        DefaultedList<ItemStack> itemList = BackpackUtils.getItemList(polymerStack);

        if (itemList.isEmpty()) return;

        if (ServerBackpacks.isDevEnvironment)
            tooltip.add(Text.of("UUID: " + BackpackManager.getStackUUID(polymerStack)));

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

                            UUID uuid = BackpackManager.getStackUUID(context.getStack());
                            if (uuid == null) uuid = BackpackManager.createNewUUID(context.getStack());
                            blockEntity.setUuid(uuid);

                            blockEntity.setStorage();

                            if (itemStack.getCustomName() != null) {
                                blockEntity.setCustomName(itemStack.getCustomName());
                            }

                            blockEntity.markDirty();
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
    public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable ItemStack stack) {
        return stack != null ? BackpackManager.getInventory(BackpackManager.getStackUUID(stack)) : null;
    }

    @Override
    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        BackpackManager.createNewUUID(stack);
        BackpackUtils.resizeIfIncorrectSize(player, stack, this.slots);

        Optional<BackpackInstance> instance = BackpackManager.getInstance(BackpackManager.getStackUUID(stack), this.slots + BackpackUtils.getExtendedSlots(stack));
        instance.ifPresent(backpackInstance -> new BackpackGui(player, stack, backpackInstance));
    }

    @Override
    public void afterChanged(ItemStack stack, Inventory inventory) {
        UUID uuid = BackpackManager.getStackUUID(stack);
        if (uuid != null) BackpackManager.addBackpack(uuid, (BackpackInventory) inventory);
    }

    public DefaultedList<ItemStack> getComponentItemList(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.slots, ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
        return list;
    }

    public static void playOpenSound(ServerPlayerEntity player) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public static void playInsertSound(ServerPlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.8F, pitch + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public static void playDropContentsSound(ServerPlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.8F, pitch + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public static void playInsertFailSound(ServerPlayerEntity player) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

}

