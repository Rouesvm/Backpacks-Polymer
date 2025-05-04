package com.rouesvm.servback.registry;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.blocks.BackpackBlock;
import com.rouesvm.servback.blocks.BasicPolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class BackpackBlockRegistry {
    // how do I read this
    public static final Block BACKPACK = register("backpack", new BackpackBlock("backpack"));

    public static Block register(String name, BasicPolymerBlock block) {
        return Registry.register(Registries.BLOCK, Identifier.of(Main.MOD_ID, name), block);
    }

    public static void initialize() {}
}
