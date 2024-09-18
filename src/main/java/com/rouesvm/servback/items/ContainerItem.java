package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.ui.GlobalBackpackGui;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.core.api.utils.PolymerKeepModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

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

class BasicPolymerItem extends Item implements PolymerItem, PolymerKeepModel {
    private final String name;
    private final PolymerModelData model;

    public BasicPolymerItem(String name) {
        super(new Settings().maxCount(1));
        this.name = name;
        this.model = PolymerResourcePackUtils.requestModel(Items.LEATHER,
                Identifier.of(Main.MOD_ID , "item/" + getIdentifier().getPath()));
    }

    public Identifier getIdentifier() {
        return Identifier.of(Main.MOD_ID, getActualName().toLowerCase());
    }

    public String getActualName() {
        return this.name;
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.model.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.model.value();
    }
}
