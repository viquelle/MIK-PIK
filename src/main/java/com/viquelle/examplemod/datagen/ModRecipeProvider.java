package com.viquelle.examplemod.datagen;

import com.viquelle.examplemod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.LIGHTER.get())
                .pattern("ISF")
                .pattern("ICI")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('S', Items.STRING)
                .define('F', Items.FLINT)
                .define('C', Items.COAL)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(recipeOutput);
    }
}
