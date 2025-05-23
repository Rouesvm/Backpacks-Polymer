package com.rouesvm.servback.registry;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.block.backpack.BackpackBlockEntity;
import com.rouesvm.servback.block.basic.EnderBlockEntity;
import com.rouesvm.servback.block.basic.GlobalBlockEntity;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BackpackBlockEntityRegistry {
    public static final BlockEntityType<BackpackBlockEntity> BACKPACK_BLOCK_ENTITY = register(
            "backpack_block_entity",
            FabricBlockEntityTypeBuilder.create(BackpackBlockEntity::new, BackpackBlockRegistry.BACKPACK).build());

    public static final BlockEntityType<EnderBlockEntity> ENDER_BACKPACK_BLOCK_ENTITY = register(
            "ender_backpack_block_entity",
            FabricBlockEntityTypeBuilder.create(EnderBlockEntity::new, BackpackBlockRegistry.ENDER_BACKPACK).build());

    public static final BlockEntityType<GlobalBlockEntity> GLOBAL_BACKPACK_BLOCK_ENTITY = register(
            "global_backpack_block_entity",
            FabricBlockEntityTypeBuilder.create(GlobalBlockEntity::new, BackpackBlockRegistry.GLOBAL_BACKPACK).build());

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> blockEntityType) {
        var entity = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(Main.MOD_ID, name), blockEntityType);
        PolymerBlockUtils.registerBlockEntity(entity);
        return entity;
    }

    public static void initialize() {
        ItemStorage.SIDED.registerForBlockEntity(BackpackBlockEntity::getInventoryProvider, BACKPACK_BLOCK_ENTITY);
    }
}
