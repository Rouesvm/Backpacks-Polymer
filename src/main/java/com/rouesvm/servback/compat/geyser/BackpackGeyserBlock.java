package com.rouesvm.servback.compat.geyser;

import com.rouesvm.servback.compat.geyser.bedrock.BedrockBlock;
import com.rouesvm.servback.content.block.impl.BackpackBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.geysermc.geyser.api.block.custom.CustomBlockData;
import org.geysermc.geyser.api.block.custom.CustomBlockPermutation;
import org.geysermc.geyser.api.block.custom.CustomBlockState;
import org.geysermc.geyser.api.block.custom.NonVanillaCustomBlockData;
import org.geysermc.geyser.api.block.custom.component.*;
import org.geysermc.geyser.api.block.custom.nonvanilla.JavaBlockState;
import org.geysermc.geyser.api.block.custom.nonvanilla.JavaBoundingBox;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BackpackGeyserBlock {
    public static final List<String> dye_colors = Arrays.stream(DyeColor.values()).map(DyeColor::getSerializedName).toList();
    public static final List<String> facing = HorizontalDirectionalBlock.FACING.getPossibleValues().stream().map(Direction::getSerializedName).toList();
    public static final List<Integer> slots = new ArrayList<>();

    static {
        slots.add(1);
        slots.add(2);
        slots.add(3);
    }

    public static final String STATE_CONDITION = "query.block_property('%s') == %s";

    public static void onGeyserDefineCustomBlocksEvent(GeyserDefineCustomBlocksEvent event) {
        BuiltInRegistries.BLOCK.entrySet().stream()
                .filter(entry -> entry.getValue() instanceof BedrockBlock)
                .forEach(entry -> {
                    Identifier location = entry.getKey().identifier();
                    Block block = entry.getValue();

                    NonVanillaCustomBlockData customBlockData = createHorizontalBlock(block, location)
                            .permutations(createHorizontalBlockPermutations())
                            .build();

                    if (block instanceof BackpackBlock) customBlockData = registerBackpackBlock(block, location);

                    event.register(customBlockData);
                    registerForBlockState(event, customBlockData, block, location);
                });
    }

    public static void registerForBlockState(GeyserDefineCustomBlocksEvent event, NonVanillaCustomBlockData customBlockData, Block block, Identifier location) {
        int blockId = BuiltInRegistries.BLOCK.getId(block);
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            CustomBlockState.Builder stateBuilder = blockStateBuilder(customBlockData, state);

            JavaBlockState javaBlockState = JavaBlockState.builder()
                    .identifier(location.toString())
                    .blockHardness(block.defaultDestroyTime())
                    .canBreakWithHand(true)
                    .collision(new JavaBoundingBox[]{new JavaBoundingBox(0, 0, 0, 1, 1, 1)})
                    .javaId(Block.getId(state))
                    .stateGroupId(blockId)
                    .build();

            event.registerOverride(javaBlockState, stateBuilder.build());
        }
    }

    public static CustomBlockState.Builder blockStateBuilder(CustomBlockData customBlockData, BlockState state) {
        CustomBlockState.Builder stateBuilder = customBlockData.blockStateBuilder();
        for (Property<?> property : state.getProperties()) {
            switch (property) {
                case IntegerProperty intProperty ->
                        stateBuilder.intProperty(property.getName(), state.getValue(intProperty));
                case BooleanProperty booleanProperty ->
                        stateBuilder.booleanProperty(property.getName(), state.getValue(booleanProperty));
                case EnumProperty<?> enumProperty ->
                        stateBuilder.stringProperty(enumProperty.getName(), state.getValue(enumProperty).getSerializedName());
                default -> throw new IllegalArgumentException("Unknown property type: " + property.getClass().getName());
            }
        }

        return stateBuilder;
    }

    private static NonVanillaCustomBlockData registerBackpackBlock(Block block, Identifier location) {
        return createHorizontalBlock(block, location)
                .booleanProperty(BackpackBlock.HAS_DYE.getName())
                .intProperty(BackpackBlock.SLOTS.getName(), slots)
                .stringProperty(BackpackBlock.DYE_COLOR.getName(), dye_colors)
                .permutations(createBackpackPermutations())
                .name(location.getPath())
                .namespace(location.getNamespace())
                .build();
    }

    private static NonVanillaCustomBlockData.Builder createHorizontalBlock(Block block, Identifier location) {
        BoxComponent collisionBox = BoxComponent.fullBox();
        BoxComponent selectionBox = BoxComponent.fullBox();

        CustomBlockComponents components = CustomBlockComponents.builder()
                .collisionBox(collisionBox)
                .selectionBox(selectionBox)
                .materialInstance("*", MaterialInstance.builder()
                        .texture("serverbackpacks:" + location.getPath())
                        .renderMethod("opaque")
                        .faceDimming(true)
                        .ambientOcclusion(true)
                        .build())
                .geometry(GeometryComponent.builder()
                        .identifier("geometry.backpack_3")
                        .build())
                .collisionBox(collisionBox)
                .selectionBox(selectionBox)
                .lightEmission(block.defaultBlockState().getLightEmission())
                .lightDampening(block.defaultBlockState().getLightDampening())
                .destructibleByMining(block.defaultDestroyTime())
                .friction(Math.min(1 - block.getFriction(), 0.9f))
                .build();

        return NonVanillaCustomBlockData.builder()
                .stringProperty(BackpackBlock.FACING.getName(), facing)
                .name(location.getPath())
                .namespace(location.getNamespace())
                .components(components);
    }

    private static List<CustomBlockPermutation> createHorizontalBlockPermutations() {
        List<CustomBlockPermutation> permutations = new ArrayList<>();

        for (String direction : facing) {
            // got lazy
            int yRot;

            switch (direction) {
                case "south" -> yRot = 180;
                case "west" -> yRot = 270;
                case "east" -> yRot = 90;
                default -> yRot = 0;
            }

            CustomBlockComponents customBlockComponents = CustomBlockComponents.builder()
                    .transformation(new TransformationComponent(
                            0,
                            (360 - yRot) % 360,
                            0
                    ))
                    .build();

            permutations.add(new CustomBlockPermutation(customBlockComponents, String.format(STATE_CONDITION,
                    BackpackBlock.FACING.getName(), "'" + direction.toLowerCase() + "'")));
        }
        return permutations;
    }

    private static List<CustomBlockPermutation> createBackpackPermutations() {
        List<CustomBlockPermutation> permutations = new ArrayList<>(createHorizontalBlockPermutations());

        for (int size : slots) {
            permutations.addAll(createDyePermutations(size));
            permutations.add(createGeometryPermutation(size));
        }

        return permutations;
    }

    private static List<CustomBlockPermutation> createDyePermutations(int size) {
        List<CustomBlockPermutation> dyePermutations = new ArrayList<>();

        for (String dyeColor : dye_colors) {
            CustomBlockComponents customBlockComponents = CustomBlockComponents.builder()
                    .materialInstance("*", MaterialInstance.builder()
                            .texture("serverbackpacks:" + dyeColor + "_" + size)
                            .renderMethod("opaque")
                            .faceDimming(true)
                            .ambientOcclusion(true)
                            .build())
                    .build();

            dyePermutations.add(new CustomBlockPermutation(
                    customBlockComponents,
                    String.format(STATE_CONDITION, BackpackBlock.SLOTS.getName(), size) + " && " +
                            String.format(STATE_CONDITION, BackpackBlock.DYE_COLOR.getName(), "'" + dyeColor.toLowerCase() + "'") + " && " +
                            String.format(STATE_CONDITION, BackpackBlock.HAS_DYE.getName(), "true")
            ));
        }

        CustomBlockComponents basicComponents = CustomBlockComponents.builder()
                .materialInstance("*", MaterialInstance.builder()
                        .texture("serverbackpacks:" + size)
                        .renderMethod("opaque")
                        .faceDimming(true)
                        .ambientOcclusion(true)
                        .build())
                .build();

        dyePermutations.add(new CustomBlockPermutation(
                basicComponents,
                String.format(STATE_CONDITION, BackpackBlock.SLOTS.getName(), size) + " && " +
                        String.format(STATE_CONDITION, BackpackBlock.HAS_DYE.getName(), "false")
        ));

        return dyePermutations;
    }

    private static CustomBlockPermutation createGeometryPermutation(int size) {
        CustomBlockComponents customBlockComponents = CustomBlockComponents.builder()
                .geometry(GeometryComponent.builder()
                        .identifier("geometry.backpack_" + size)
                        .build())
                .build();

        return new CustomBlockPermutation(customBlockComponents,
                String.format(STATE_CONDITION, BackpackBlock.SLOTS.getName(), size));
    }
}
