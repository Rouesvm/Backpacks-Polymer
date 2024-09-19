package com.rouesvm.servback.items;

import com.rouesvm.servback.ui.BackpackGui;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ContainerItem extends BasicPolymerItem {
    private final int slots;

    public ContainerItem(String id, int slots) {
        super(id);
        this.slots = slots;
    }

    @Override
    public Text getName() {
        return Text.of(getActualName() + " Backpack");
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

        createGui(serverPlayer, stack);
        return TypedActionResult.success(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getPlayer() instanceof ServerPlayerEntity player))
            return ActionResult.PASS;
        if (player.isSneaking())
            return ActionResult.PASS;

        createGui(player, context.getStack());
        return ActionResult.PASS;
    }

    public void createGui(ServerPlayerEntity player, ItemStack stack) {
        new BackpackGui(player, stack, getInventory(stack));
    }

    public Inventory getInventory(ItemStack stack) {
        ContainerComponent component = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);

        DefaultedList<ItemStack> list = DefaultedList.ofSize(slots, ItemStack.EMPTY);
        component.copyTo(list);

        return new SimpleInventory(list.toArray(ItemStack[]::new));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        ContainerComponent containerComponent = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);

        int i = 0;
        int j = 0;
        for (ItemStack itemStack : containerComponent.iterateNonEmpty()) {
            j++;
            if (i <= 4) {
                i++;
                tooltip.add(Text.translatable("container.shulkerBox.itemCount", itemStack.getName(), itemStack.getCount()).formatted(Formatting.GOLD));
            }
        }

        if (j - i > 0) {
            tooltip.add(Text.translatable("container.shulkerBox.more", j - i).formatted(Formatting.ITALIC).formatted(Formatting.GOLD));
        }
    }
}

