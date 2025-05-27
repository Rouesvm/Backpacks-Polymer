package com.rouesvm.servback.compat.geyser;

import com.rouesvm.servback.block.backpack.BackpackBlock;
import com.rouesvm.servback.utils.bedrock.BedrockBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
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
    public static List<String> dye_colors = Arrays.stream(DyeColor.values()).map(DyeColor::asString).toList();
    public static List<String> facing = HorizontalFacingBlock.FACING.getValues().stream().map(Direction::asString).toList();
    public static List<Integer> slots = new ArrayList<>();

    static {
        slots.add(1);
        slots.add(2);
        slots.add(3);
    }

    public static String STATE_CONDITION = "query.block_property('%s') == %s";

    public static void onGeyserDefineCustomBlocksEvent(GeyserDefineCustomBlocksEvent event) {
        Registries.BLOCK.getEntrySet().stream()
                .filter(entry -> entry.getValue() instanceof BedrockBlock)
                .forEach(entry -> {
                    Identifier location = entry.getKey().getValue();
                    Block block = entry.getValue();

                    NonVanillaCustomBlockData customBlockData = createHorizontalBlock(block, location)
                            .permutations(createHorizontalBlockPermutations())
                            .build();

                    if (block instanceof BackpackBlock) customBlockData = registerBackpackBlock(block, location);

                    event.register(customBlockData);

                    int blockId = Registries.BLOCK.getRawId(block);
                    for (BlockState state : block.getStateManager().getStates()) {
                        CustomBlockState.Builder stateBuilder = customBlockData.blockStateBuilder();
                        for (Property<?> property : state.getProperties()) {
                            if (property instanceof IntProperty intProperty) {
                                stateBuilder.intProperty(property.getName(), state.get(intProperty));
                            } else if (property instanceof BooleanProperty booleanProperty) {
                                stateBuilder.booleanProperty(property.getName(), state.get(booleanProperty));
                            } else if (property instanceof EnumProperty<?> enumProperty) {
                                stateBuilder.stringProperty(enumProperty.getName(), state.get(enumProperty).asString());
                            } else {
                                throw new IllegalArgumentException("Unknown property type: " + property.getClass().getName());
                            }
                        }

                        JavaBlockState javaBlockState = JavaBlockState.builder()
                                .identifier(location.toString())
                                .blockHardness(block.getHardness())
                                .canBreakWithHand(true)
                                .collision(new JavaBoundingBox[]{new JavaBoundingBox(0, 0, 0, 1, 1, 1)})
                                .javaId(Block.getRawIdFromState(state))
                                .stateGroupId(blockId)
                                .build();

                        event.registerOverride(javaBlockState, stateBuilder.build());
                    }
                });
    }

    private static NonVanillaCustomBlockData registerBackpackBlock(Block block, Identifier location) {
        NonVanillaCustomBlockData data = createHorizontalBlock(block, location)
                .intProperty(BackpackBlock.SLOTS.getName(), slots)
                .stringProperty(BackpackBlock.DYE_COLOR.getName(), dye_colors)
                .permutations(createBackpackPermutations())
                .name(location.getPath())
                .namespace(location.getNamespace())
                .build();
        return data;
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
                .lightEmission(block.getDefaultState().getLuminance())
                .lightDampening(block.getDefaultState().getOpacity())
                .friction(Math.min(1 - block.getSlipperiness(), 0.9f))
                .build();

        NonVanillaCustomBlockData.Builder data = NonVanillaCustomBlockData.builder()
                .stringProperty(BackpackBlock.FACING.getName(), facing)
                .name(location.getPath())
                .namespace(location.getNamespace())
                .components(components);

        return data;
    }

    private static List<CustomBlockPermutation> createHorizontalBlockPermutations() {
        List<CustomBlockPermutation> permutations = new ArrayList<>();

        for (String direction : facing) {
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
        List<CustomBlockPermutation> permutations = new ArrayList<>();
        permutations.addAll(createHorizontalBlockPermutations());

        for (int size : slots) {
            for (String dyeColor : dye_colors) {
                CustomBlockComponents customBlockComponents = CustomBlockComponents.builder()
                        .materialInstance("*", MaterialInstance.builder()
                                .texture("serverbackpacks:" + dyeColor + "_" + size)
                                .renderMethod("opaque")
                                .faceDimming(true)
                                .ambientOcclusion(true)
                                .build())
                        .build();

                permutations.add(new CustomBlockPermutation(customBlockComponents, String.format(STATE_CONDITION,
                        BackpackBlock.SLOTS.getName(), size) + " && " + String.format(STATE_CONDITION,
                        BackpackBlock.DYE_COLOR.getName(), "'" + dyeColor.toLowerCase() + "'")));
            }

            CustomBlockComponents customBlockComponents = CustomBlockComponents.builder()
                    .geometry(GeometryComponent.builder()
                            .identifier("geometry.backpack_" + size)
                            .build())
                    .build();

            permutations.add(new CustomBlockPermutation(customBlockComponents, String.format(STATE_CONDITION,
                    BackpackBlock.SLOTS.getName(), size)));
        }

        return permutations;
    }
}
