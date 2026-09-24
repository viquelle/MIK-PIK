package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.ICampfireFuel;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.datagen.ModConfig;
import com.viquelle.mikpik.util.CampfireCookingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin extends BlockEntity implements ICampfireFuel {
    public CampfireBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Unique private int mikpik$maxFuelTime = ModConfig.MAX_CAMP_FUEL_TIME.get();
    @Unique private int mikpik$fuelTime = CampfireCookingHelper.INITIAL_FUEL_TIME;
    @Unique private long mikpik$lastUpdate = -1;

    @Unique
    public int mikpik$getFuelTime() { return mikpik$fuelTime; }

    @Unique
    public void mikpik$setLastUpdate(long time) { mikpik$lastUpdate = time; }

    @Unique
    public int mikpik$addFuel(int ticks) {
        mikpik$fuelTime = Math.clamp(mikpik$fuelTime + ticks, 0, mikpik$maxFuelTime);
        this.setChanged();
        return mikpik$fuelTime;
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void mikpik$saveFuel(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putInt("mikpik_fuelTime", mikpik$fuelTime);
        tag.putLong("mikpik_lastUpdate", mikpik$lastUpdate);
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void mikpik$loadFuel(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        mikpik$fuelTime = tag.getInt("mikpik_fuelTime");
        mikpik$lastUpdate = tag.getLong("mikpik_lastUpdate");
    }

    @Inject(method = "getUpdateTag", at = @At("RETURN"))
    private void mikpik$updateTagFuel(HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putInt("mikpik_fuelTime", mikpik$fuelTime);
    }
}
