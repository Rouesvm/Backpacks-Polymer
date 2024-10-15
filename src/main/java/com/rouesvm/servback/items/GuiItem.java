package com.rouesvm.servback.items;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class GuiItem extends BasicPolymerItem {
    public GuiItem(String name) {
        super(name, Items.LEATHER);
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

        return TypedActionResult.success(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!(context.getPlayer() instanceof ServerPlayerEntity serverPlayer))
            return ActionResult.PASS;
        if (serverPlayer.isSneaking())
            return ActionResult.PASS;

        openGui(serverPlayer, context.getStack());
        return ActionResult.success(true);
    }

    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        new SimpleGui(ScreenHandlerType.ANVIL, player, false);
    }
}
