package com.rouesvm.servback.content.item;

import com.rouesvm.servback.content.block.BasicBackpackBlockEntity;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.ui.BasicInventoryGui;
import com.rouesvm.servback.technical.ui.UpgradeContainerGui;
import com.rouesvm.servback.technical.ui.inventory.BaseInventory;
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
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
public class BundleGuiItem extends BasicPolymerBlockItem  {
    public BundleGuiItem(String name, Block block) {
        super(name, Items.LEATHER, block);
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
        if (Configuration.isDisabled(stack.getItem())
        ) tooltip.add(Text.translatable("tooltip.serverbackpacks.disabled")
                .formatted(Formatting.BOLD)
                .formatted(Formatting.RED));
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        if (world.getBlockEntity(pos) instanceof BasicBackpackBlockEntity blockEntity) {
            blockEntity.setItem(this);
            blockEntity.setSize(BackpackUtils.getExtendedSlots(stack));

            if (stack.getName() != null
            ) blockEntity.setCustomName(stack.getName());

            blockEntity.markDirty();
            ContainerItem.playOpenSound((ServerPlayerEntity) player);
        }

        return writeNbtToBlockEntity(world, player, pos, stack);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        var cast = player.raycast(5,0,false);
        if (!(player instanceof ServerPlayerEntity serverPlayer)
        ) return TypedActionResult.pass(stack);

        if (cast.getType() == HitResult.Type.BLOCK
        ) return TypedActionResult.pass(stack);

        if (player.isSneaking()) {
            if (stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER) != null) {
                new UpgradeContainerGui(serverPlayer, stack);
                return TypedActionResult.success(stack);
            } else return TypedActionResult.pass(stack);
        }

        onOpenGui(serverPlayer, stack);
        player.swingHand(hand, true);
        return TypedActionResult.success(stack);
    }

    @Override
    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        return Configuration.instance().placeable && super.canPlace(context, state);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getPlayer() instanceof ServerPlayerEntity serverPlayer)
        ) return ActionResult.PASS;
        if (Configuration.instance().placeable
                && serverPlayer.isSneaking()
        ) return super.useOnBlock(context);

        onOpenGui(serverPlayer, context.getStack());
        serverPlayer.swingHand(context.getHand(), true);
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        Inventory inventory = getInventory(serverPlayer, stack);

        if (inventory != null) {
            ItemStack itemStack = slot.getStack();

            if (slot instanceof CraftingResultSlot) return false;
            if (!itemStack.getItem().canBeNested()) return false;

            if (clickType == ClickType.LEFT && !itemStack.isEmpty()) {
                if (BaseInventory.canInsert(itemStack, (BaseInventory) inventory)) {
                    itemStack = BaseInventory.addStack(itemStack, inventory);
                    ContainerItem.playInsertSound(serverPlayer, 0.8F);
                } else ContainerItem.playInsertFailSound(serverPlayer);

                slot.setStack(itemStack);
                afterChanged(stack, inventory);
                onContentChanged(player);
                return true;
            } else return false;
        } else return false;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.LEFT && otherStack.isEmpty()) {
        } else {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            Inventory inventory = getInventory(serverPlayer, stack);

            if (slot instanceof CraftingResultSlot) return false;
            if (!otherStack.getItem().canBeNested()) return false;

            if (clickType == ClickType.RIGHT) {
                onOpenGui(serverPlayer, stack);
                return true;
            }

            if (inventory != null) {
                if (clickType == ClickType.LEFT && !otherStack.isEmpty()) {
                    if (BaseInventory.canInsert(otherStack, (BaseInventory) inventory)) {
                        otherStack = BaseInventory.addStack(otherStack, inventory);
                        ContainerItem.playInsertSound(serverPlayer, 0.8F);
                    } else ContainerItem.playInsertFailSound(serverPlayer);

                    cursorStackReference.set(otherStack);
                    afterChanged(stack, inventory);
                    onContentChanged(player);
                    return true;
                }
            }
        }
        return false;
    }

    public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable ItemStack stack) {
        return null;
    }

    public void afterChanged(ItemStack stack, Inventory inventory) {
    }

    public void onContentChanged(PlayerEntity user) {
        ScreenHandler screenHandler = user.currentScreenHandler;
        if (screenHandler != null
        ) screenHandler.onContentChanged(user.getInventory());
    }

    public void onOpenGui(ServerPlayerEntity player, ItemStack stack) {
        ContainerItem.playOpenSound(player);
        openGui(player, stack);
    }

    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        new BasicInventoryGui(player, stack, getInventory(player, stack));
    }
}
