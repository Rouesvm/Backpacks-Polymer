package com.rouesvm.servback.registry.block;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.block.BasicBackpackBlockEntity;
import com.rouesvm.servback.content.block.impl.BackpackBlockEntity;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BackpackBlockEntityRegistry {
    public static final BlockEntityType<BackpackBlockEntity> BACKPACK_BLOCK_ENTITY = register(
            "backpack_block_entity",
            FabricBlockEntityTypeBuilder.create(BackpackBlockEntity::new,
                    BackpackBlockRegistry.BACKPACK,
                    BackpackBlockRegistry.LAVA_BACKPACK,
                    BackpackBlockRegistry.GLOBAL_BACKPACK).build());

    public static final BlockEntityType<BasicBackpackBlockEntity> BASIC_BACKPACK_BLOCK_ENTITY = register(
            "basic_backpack_block_entity",
            FabricBlockEntityTypeBuilder.create(BasicBackpackBlockEntity::new)
                    .addBlocks(BackpackBlockRegistry.ENDER_BACKPACK).build());

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> blockEntityType) {
        var entity = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(ServerBackpacks.MOD_ID, name), blockEntityType);
        PolymerBlockUtils.registerBlockEntity(entity);
        return entity;
    }

    public static void initialize() {
        ItemStorage.SIDED.registerForBlockEntity(BackpackBlockEntity::getInventoryProvider, BACKPACK_BLOCK_ENTITY);
    }
}
