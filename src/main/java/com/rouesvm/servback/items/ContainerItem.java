package com.rouesvm.servback.items;

import com.rouesvm.servback.blocks.BackpackBlockEntity;
import com.rouesvm.servback.registry.BackpackBlockEntityRegistry;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import xyz.nucleoid.packettweaker.PacketContext;

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

                            NbtCompound nbtCompound = new NbtCompound();
                            Text name = itemStack.get(DataComponentTypes.ITEM_NAME);
                            if (name != null) nbtCompound.putString("custom_name", name.getString());

                            setBlockEntityData(itemStack, BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY, nbtCompound);
                        }

                        if (playerEntity instanceof ServerPlayerEntity) {
                            Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)playerEntity, blockPos, itemStack);
                        }
                    }

                    BlockSoundGroup blockSoundGroup = blockState2.getSoundGroup();
                    world.playSound(playerEntity, blockPos, this.getPlaceSound(blockState2), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
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
            item = getBackpackMap(color, size / 9);
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

