package com.rouesvm.servback.registry.block;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.block.BasicBackpackBlockEntity;
import com.rouesvm.servback.content.block.impl.BackpackBlockEntity;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class BackpackBlockEntityRegistry {
    public static final BlockEntityType<BackpackBlockEntity> BACKPACK_BLOCK_ENTITY = register(
            "backpack_block_entity",
            FabricBlockEntityTypeBuilder.create(BackpackBlockEntity::new, BackpackBlockRegistry.BACKPACK).build());

    public static BlockEntityType<BasicBackpackBlockEntity> BASIC_BACKPACK_BLOCK_ENTITY;

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> blockEntityType) {
        var entity = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(ServerBackpacks.MOD_ID, name), blockEntityType);
        PolymerBlockUtils.registerBlockEntity(entity);
        return entity;
    }

    public static void initialize() {
        ItemStorage.SIDED.registerForBlockEntity(BackpackBlockEntity::getInventoryProvider, BACKPACK_BLOCK_ENTITY);

        if (BackpackBlockRegistry.GLOBAL_BACKPACK != null || BackpackBlockRegistry.ENDER_BACKPACK != null) {
            var builder = FabricBlockEntityTypeBuilder.create(BasicBackpackBlockEntity::new);
            List<Block> toBeAdded = new ArrayList<>();
            if (BackpackBlockRegistry.GLOBAL_BACKPACK != null) toBeAdded.add(BackpackBlockRegistry.GLOBAL_BACKPACK);
            if (BackpackBlockRegistry.ENDER_BACKPACK != null) toBeAdded.add(BackpackBlockRegistry.ENDER_BACKPACK);

            builder.addBlocks(toBeAdded);
            BASIC_BACKPACK_BLOCK_ENTITY = register("basic_backpack_block_entity", builder.build());
        }
    }
}
