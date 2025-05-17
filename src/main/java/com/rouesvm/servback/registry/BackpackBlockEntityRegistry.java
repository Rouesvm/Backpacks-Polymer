package com.rouesvm.servback.registry;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.block.BackpackBlockEntity;
import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BackpackBlockEntityRegistry {
    public static final BlockEntityType<BackpackBlockEntity> BACKPACK_BLOCK_ENTITY = register(
            "backpack_block_entity",
            FabricBlockEntityTypeBuilder.create(BackpackBlockEntity::new, BackpackBlockRegistry.BACKPACK).build());

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType<T> blockEntityType) {
        var entity = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(Main.MOD_ID, name), blockEntityType);
        PolymerBlockUtils.registerBlockEntity(entity);
        return entity;
    }

    public static void initialize() {}
}
