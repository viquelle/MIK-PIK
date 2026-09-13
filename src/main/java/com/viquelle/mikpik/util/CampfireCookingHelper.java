package com.viquelle.mikpik.util;

import com.viquelle.mikpik.datagen.ModConfig;
import com.viquelle.mikpik.item.FreshnessManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class CampfireCookingHelper {
    public static final int MAX_COOK_TIME = 120;
    public static final int INITIAL_FUEL_TIME = ModConfig.INITIAL_CAMP_FUEL_TIME.get();

    public static RecipeHolder<CampfireCookingRecipe> getCookingRecipe(Level level, ItemStack stack) {
        return level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, new SingleRecipeInput(stack), level).orElse(null);
    }

    public static int getFuelValue(ItemStack stack) {
        return ModConfig.getFuelValue(stack.getItem());
    }

    public static ItemStack processCookedItem(ItemStack input, ItemStack recipeResult, RegistryAccess registryAccess) {
        ItemStack result = recipeResult.copy();
        int spoilTime = FreshnessManager.shouldSpoiling(result);
        if (spoilTime <= 0) return result;

        float newPercent = Math.clamp(FreshnessManager.getSpoilPercent(input) + 0.4f,0f,1f);
        FreshnessManager.applySpoilData(result,spoilTime, (int)(spoilTime * newPercent));
        return result;
    }
}

