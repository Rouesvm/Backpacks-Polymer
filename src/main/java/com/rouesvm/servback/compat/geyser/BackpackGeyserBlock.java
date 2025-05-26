package com.rouesvm.servback.compat.geyser;

import com.rouesvm.servback.block.backpack.BackpackBlock;
import com.rouesvm.servback.utils.bedrock.BedrockBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.geysermc.geyser.api.block.custom.CustomBlockPermutation;
import org.geysermc.geyser.api.block.custom.NonVanillaCustomBlockData;
import org.geysermc.geyser.api.block.custom.component.BoxComponent;
import org.geysermc.geyser.api.block.custom.component.CustomBlockComponents;
import org.geysermc.geyser.api.block.custom.component.GeometryComponent;
import org.geysermc.geyser.api.block.custom.component.MaterialInstance;
import org.geysermc.geyser.api.block.custom.nonvanilla.JavaBlockState;
import org.geysermc.geyser.api.block.custom.nonvanilla.JavaBoundingBox;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class BackpackGeyserBlock {
    public static List<String> dye_colors = Arrays.stream(DyeColor.values()).map(DyeColor::asString).toList();
    public static List<String> facing = HorizontalFacingBlock.FACING.getValues().stream().map(Direction::asString).toList();
    public static List<Integer> slots = IntStream.range(1, 4).boxed().toList();

    public static String STATE_CONDITION = "query.block_property('%s') == %s";

    public static void onGeyserDefineCustomBlocksEvent(GeyserDefineCustomBlocksEvent event) {
        Registries.BLOCK.getEntrySet().stream()
                .filter(entry -> entry.getValue() instanceof BedrockBlock)
                .forEach(entry -> {
                    Identifier location = entry.getKey().getValue();
                    Block block = entry.getValue();

                    if (!(block instanceof BackpackBlock)) return;

                    BoxComponent collisionBox = BoxComponent.fullBox();
                    BoxComponent selectionBox = BoxComponent.fullBox();

                    CustomBlockComponents components = CustomBlockComponents.builder()
                            .collisionBox(collisionBox)
                            .selectionBox(selectionBox)
                            .geometry(GeometryComponent.builder()
                                    .identifier("geometry.backpack_1")
                                    .build())
                            .collisionBox(collisionBox)
                            .selectionBox(selectionBox)
                            .lightEmission(block.getDefaultState().getLuminance())
                            .lightDampening(block.getDefaultState().getOpacity())
                            .friction(Math.min(1 - block.getSlipperiness(), 0.9f))
                            .build();

                    NonVanillaCustomBlockData data = NonVanillaCustomBlockData.builder()
                            .intProperty(BackpackBlock.SLOTS.getName(), slots)
                            .stringProperty(BackpackBlock.FACING.getName(), facing)
                            .stringProperty(BackpackBlock.DYE_COLOR.getName(), dye_colors)
                            .permutations(createBackpackPermutations(block))
                            .components(components)
                            .name(location.getPath())
                            .namespace(location.getNamespace())
                            .build();

                    int blockId = Registries.BLOCK.getRawId(block);
                    event.register(data);

                    for (String facing : facing) {
                        for (String dye_color : dye_colors) {
                            for (int size : slots) {

                                BlockState state = block.getDefaultState()
                                        .with(BackpackBlock.FACING, Direction.byId(facing))
                                        .with(BackpackBlock.DYE_COLOR, DyeColor.byId(dye_color, null))
                                        .with(BackpackBlock.SLOTS, size);

                                int rawID = Block.getRawIdFromState(state);

                                JavaBlockState facingStates = JavaBlockState.builder()
                                        .identifier(location.toString())
                                        .blockHardness(block.getHardness())
                                        .canBreakWithHand(true)
                                        .collision(new JavaBoundingBox[]{new JavaBoundingBox(0, 0, 0, 1, 1, 1)})
                                        .javaId(rawID)
                                        .stateGroupId(blockId)
                                        .build();

                                event.registerOverride(facingStates, data.blockStateBuilder()
                                        .stringProperty(BackpackBlock.DYE_COLOR.getName(), dye_color)
                                        .stringProperty(BackpackBlock.FACING.getName(), facing)
                                        .intProperty(BackpackBlock.SLOTS.getName(), size)
                                        .build());
                            }
                        }
                    }
                });
    }


    private static List<CustomBlockPermutation> createBackpackPermutations(Block block) {
        List<CustomBlockPermutation> permutations = new ArrayList<>();

        for (int size : slots) {
            for (String dyeColor : dye_colors) {
                MaterialInstance materialInstance = MaterialInstance.builder()
                        .texture("serverbackpacks:red_3")
                        .renderMethod("opaque")
                        .faceDimming(true)
                        .ambientOcclusion(true)
                        .build();

                CustomBlockComponents customBlockComponents = CustomBlockComponents.builder()
                        .materialInstance("*", materialInstance)
                        .build();

                permutations.add(new CustomBlockPermutation(customBlockComponents, String.format(STATE_CONDITION,
                        BackpackBlock.DYE_COLOR.getName(), "'" + dyeColor.toLowerCase() + "'")));
            }

            GeometryComponent geometryComponent = GeometryComponent.builder()
                    .identifier("geometry.backpack_" + size)
                    .build();

            CustomBlockComponents customBlockComponents = CustomBlockComponents.builder()
                    .geometry(geometryComponent)
                    .build();

            permutations.add(new CustomBlockPermutation(customBlockComponents, String.format(STATE_CONDITION,
                    BackpackBlock.SLOTS.getName(), size)));
        }

        return permutations;
    }
}
