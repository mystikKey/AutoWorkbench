package net.lockf.autoworkbenchmod.datagen;

import net.lockf.autoworkbenchmod.block.ModBlocks;
import net.lockf.autoworkbenchmod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WOODGEAR.get())
                .pattern(" s ")
                .pattern("s s")
                .pattern(" s ")
                .define('s', Items.STICK)
                .unlockedBy(getHasName(Items.STICK),has(Items.STICK))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STONEGEAR.get())
                .pattern(" c ")
                .pattern("cwc")
                .pattern(" c ")
                .define('c', Items.COBBLESTONE)
                .define('w',ModItems.WOODGEAR.get())
                .unlockedBy(getHasName(Items.STICK),has(Items.STICK))
                .unlockedBy(getHasName(Items.COBBLESTONE),has(Items.COBBLESTONE))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRONGEAR.get())
                .pattern(" i ")
                .pattern("isi")
                .pattern(" i ")
                .define('i', Items.IRON_INGOT)
                .define('s',ModItems.STONEGEAR.get())
                .unlockedBy(getHasName(Items.STICK),has(Items.STICK))
                .unlockedBy(getHasName(Items.COBBLESTONE),has(Items.COBBLESTONE))
                .unlockedBy(getHasName(Items.IRON_INGOT),has(Items.IRON_INGOT))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLDGEAR.get())
                .pattern(" g ")
                .pattern("gig")
                .pattern(" g ")
                .define('g', Items.GOLD_INGOT)
                .define('i',ModItems.IRONGEAR.get())
                .unlockedBy(getHasName(Items.STICK),has(Items.STICK))
                .unlockedBy(getHasName(Items.COBBLESTONE),has(Items.COBBLESTONE))
                .unlockedBy(getHasName(Items.IRON_INGOT),has(Items.IRON_INGOT))
                .unlockedBy(getHasName(Items.GOLD_INGOT),has(Items.GOLD_INGOT))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIAMONDGEAR.get())
                .pattern(" d ")
                .pattern("dgd")
                .pattern(" d ")
                .define('d', Items.DIAMOND)
                .define('g', ModItems.GOLDGEAR.get())
                .unlockedBy(getHasName(Items.STICK),has(Items.STICK))
                .unlockedBy(getHasName(Items.COBBLESTONE),has(Items.COBBLESTONE))
                .unlockedBy(getHasName(Items.IRON_INGOT),has(Items.IRON_INGOT))
                .unlockedBy(getHasName(Items.GOLD_INGOT),has(Items.GOLD_INGOT))
                .unlockedBy(getHasName(Items.DIAMOND),has(Items.DIAMOND))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.Auto_Workbench.get())
                .pattern("   ")
                .pattern("scs")
                .pattern("   ")
                .define('c', Items.CRAFTING_TABLE)
                .define('s', ModItems.STONEGEAR.get())
                .unlockedBy(getHasName(Items.STICK),has(Items.STICK))
                .unlockedBy(getHasName(Items.COBBLESTONE),has(Items.COBBLESTONE))
                .unlockedBy(getHasName(Items.CRAFTING_TABLE),has(Items.CRAFTING_TABLE))
                .save(consumer);
    }
}
