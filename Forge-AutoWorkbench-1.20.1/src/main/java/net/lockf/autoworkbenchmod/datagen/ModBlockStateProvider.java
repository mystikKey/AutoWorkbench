package net.lockf.autoworkbenchmod.datagen;

import net.lockf.autoworkbenchmod.AutoWorkbenchMod;
import net.lockf.autoworkbenchmod.block.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.BiConsumer;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, AutoWorkbenchMod.MOD_ID, exFileHelper);
    }

    /***
     * Add Blocks
     */
    @Override
    protected void registerStatesAndModels() {
        // For auto workbench

        final ResourceLocation BOTTOM = new ResourceLocation(AutoWorkbenchMod.MOD_ID, "block/auto_workbench_bottom");
        final ResourceLocation TOP = new ResourceLocation(AutoWorkbenchMod.MOD_ID, "block/auto_workbench_top");
        final ResourceLocation SIDE = new ResourceLocation(AutoWorkbenchMod.MOD_ID, "block/auto_workbench_side");

        registerOrientedBlock("block/auto_workbench_front", SIDE, TOP, BOTTOM);

        // examples
        //blockWithItem(ModBlocks.Auto_Workbench);
        //simpleBlockWithItem(ModBlocks.Auto_Workbench.get(), new ModelFile.UncheckedModelFile(modLoc("block/auto_workbench")));
    }

    /*
    // Not oriented block
    private void blockWithItem(RegistryObject<Block> blockRegistryObject){
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }
    */

    // Horizontal oriented blocks
    private void registerOrientedBlock(String modLocFront, ResourceLocation modLocSide, ResourceLocation modLocTop,
                                       ResourceLocation modLocBottom){
        BlockModelBuilder modelOn = models().cube(ModBlocks.Auto_Workbench.getId().getPath(), modLocBottom, modLocTop,
                modLoc(modLocFront),modLocSide,modLocSide,modLocSide).texture("particle", modLocSide);
        directionBlock(ModBlocks.Auto_Workbench.get(), (state, builder) -> {
            builder.modelFile(modelOn);
        });
    }

    // Horizontal oriented block assist
    private VariantBlockStateBuilder directionBlock(Block block, BiConsumer<BlockState, ConfiguredModel.Builder<?>> model) {
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        builder.forAllStates(state -> {
            ConfiguredModel.Builder<?> bld = ConfiguredModel.builder();
            model.accept(state, bld);
            //applyRotationBld(bld, state.getValue(BlockStateProperties.FACING)); /*For oriented blocks (all directions)*/
            applyRotationBld(bld, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
            return bld.build();
        });
        return builder;
    }

    // Horizontal oriented block assist
    private void applyRotationBld(ConfiguredModel.Builder<?> builder, Direction direction) {
        switch (direction) {
            case NORTH, UP, DOWN -> { }
            case SOUTH -> builder.rotationY(180);
            case WEST -> builder.rotationY(270);
            case EAST -> builder.rotationY(90);
            //case DOWN -> builder.rotationX(90);
            //case UP -> builder.rotationX(-90)
        }
    }
}
