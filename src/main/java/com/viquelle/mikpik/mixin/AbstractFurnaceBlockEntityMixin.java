package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.item.FreshnessManager;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
    @Shadow @Final private static int SLOT_INPUT;
    @Shadow @Final private static int SLOT_RESULT;

    @Shadow
    public static boolean canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipe, NonNullList<ItemStack> inventory, int maxStackSize, AbstractFurnaceBlockEntity furnace) {
        throw new AssertionError("Shadow method called");
    }

    @Inject(
            method = "canBurn",
            at = @At("HEAD"),
            cancellable = true
    )

    private static void mikpik$allowFurnaceSpoilStacking(
            RegistryAccess registryAccess,
            @Nullable RecipeHolder<?> recipe,
            NonNullList<ItemStack> inventory,
            int maxStackSize,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (recipe == null) return;

        ItemStack result = ((AbstractCookingRecipe) recipe.value()).assemble(
                new SingleRecipeInput(furnace.getItem(SLOT_INPUT)),
                registryAccess
        );
        if (result.isEmpty()) return;

        ItemStack output = inventory.get(SLOT_RESULT);
        if (output.isEmpty()) return;

        if (ItemStack.isSameItem(output, result)) {
            if (output.getCount() + result.getCount() <= maxStackSize) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(
            method = "burn",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void mikpik$refreshFoodOnCook(
            RegistryAccess registryAccess,
            @Nullable RecipeHolder<?> recipe,
            NonNullList<ItemStack> inventory,
            int maxStackSize,
            AbstractFurnaceBlockEntity furnace,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // ИГНОРИТЬ ЖЕЛТОЕ ПОДЧЕРКИВАНИЕ ОТ IDE!!!! ОНО ВРЕТ!!!!
        if (recipe != null && canBurn(registryAccess, recipe, inventory, maxStackSize, furnace)) {
            ItemStack itemstack = inventory.get(0);
            ItemStack itemstack1 = ((AbstractCookingRecipe)recipe.value()).assemble(new SingleRecipeInput(furnace.getItem(0)), registryAccess);
            ItemStack itemstack2 = inventory.get(2);

            int spoilTime = FreshnessManager.shouldSpoiling(itemstack1);
            if (spoilTime <= 0) return;
            float mayBeNewPercent = Math.clamp(FreshnessManager.getSpoilPercent(itemstack) + 0.33f, 0.0f, 1.0f);

            if (itemstack2.isEmpty()) {
                ItemStack result = itemstack1.copy();

                FreshnessManager.applySpoilData(result, spoilTime, spoilTime * mayBeNewPercent);
                inventory.set(2, result);
            } else if (ItemStack.isSameItem(itemstack2, itemstack1)) {
                float currentPercentSumma = FreshnessManager.getSpoilPercent(itemstack2) * itemstack2.getCount();
                currentPercentSumma += itemstack1.getCount() * (mayBeNewPercent);
                itemstack2.grow(itemstack1.getCount());
                FreshnessManager.setSpoilPercent(itemstack2, currentPercentSumma / itemstack2.getCount());
            }


            itemstack.shrink(1);
            cir.setReturnValue(true);
        } else {
            cir.setReturnValue(false);
        }
    }
}
