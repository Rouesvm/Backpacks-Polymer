package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.ui.BackpackGui;
import eu.pb4.polymer.core.api.item.PolymerItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

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
}
