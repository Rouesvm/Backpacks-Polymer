package com.rouesvm.servback.content.registry.block;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.block.BasicBackpackBlock;
import com.rouesvm.servback.content.block.BasicPolymerBlock;
import com.rouesvm.servback.content.block.backpack.BackpackBlock;
import com.rouesvm.servback.technical.data.BackpackManager;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BackpackBlockRegistry {
    public static final Block BACKPACK = register("backpack", new BackpackBlock());

    public static final Block ENDER_BACKPACK = register("ender_backpack", new BasicBackpackBlock("ender_backpack") {
        @Override
        public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable BlockEntity entity) {
            return player != null ? player.getEnderChestInventory() : null;
        }
    });
    public static final Block GLOBAL_BACKPACK = register("global_backpack", new BasicBackpackBlock("global_backpack") {
        @Override
        public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable BlockEntity entity) {
            return BackpackManager.getGlobalInventory();
        }
    });

    public static Block register(String name, BasicPolymerBlock block) {
        return Registry.register(Registries.BLOCK, Identifier.of(ServerBackpacks.MOD_ID, name), block);
    }

    @SuppressWarnings("EmptyMethod")
    public static void initialize() {}
}
