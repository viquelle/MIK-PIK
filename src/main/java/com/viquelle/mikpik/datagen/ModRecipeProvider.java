package com.viquelle.mikpik.datagen;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.FLOWER_CROWN.get())
                .define('I', Ingredient.of(ItemTags.SMALL_FLOWERS))
                .pattern("III")
                .pattern("I I")
                .pattern("III")
                .unlockedBy("has_small_flowers", has(ItemTags.FLOWERS))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HEART.get())
                .define('A', Items.LEATHER)
                .define('B', Items.STRING)
                .define('M', Items.MAGMA_BLOCK)
                .define('C', Items.HAY_BLOCK)
                .pattern("ABA")
                .pattern("CMC")
                .pattern("ABA")
                .unlockedBy("has_magma_block", has(Items.MAGMA_BLOCK))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.LIFE_INJECTOR.get())
                .requires(Items.SUGAR)
                .requires(Items.FERMENTED_SPIDER_EYE)
                .requires(Items.DANDELION)
                .unlockedBy("has_sugar", has(Items.SUGAR))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MEAT_EFFIGY.get())
                .define('A', Ingredient.of(Items.PUMPKIN, Items.CARVED_PUMPKIN))
                .define('B', Items.HAY_BLOCK)
                .define('C', Ingredient.of(ItemTags.LOGS))
                .pattern(" A ")
                .pattern("CBC")
                .pattern(" C ")
                .unlockedBy("has_hay", has(Items.HAY_BLOCK))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MAGNETLAMPE.get())
                .define('I', Items.IRON_INGOT)
                .define('C', Items.CHAIN)
                .define('G', Items.GLOWSTONE)
                .pattern("III")
                .pattern("CGI")
                .pattern("III")
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(recipeOutput);


        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HAM_BAT.get())
                .define('A', Items.PORKCHOP)
                .define('B', Items.BONE_BLOCK)
                .define('C', Items.BONE)
                .define('D', Items.STRING)
                .pattern("DCA")
                .pattern("ABD")
                .pattern(" C ")
                .unlockedBy("has_porkchop", has(Items.PORKCHOP))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.WRAPPER.get())
                .requires(Items.PAPER, 3)
                .requires(Items.HONEYCOMB, 2)
                .unlockedBy("has_honeycomb", has(Items.HONEYCOMB))
                .save(recipeOutput);
    }
}
