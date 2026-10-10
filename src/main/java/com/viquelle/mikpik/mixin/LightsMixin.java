package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.util.WorldLightMath;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BaseTorchBlock.class)
public class LightsMixin {

    @Unique
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    @Unique
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof RedstoneTorchBlock || block instanceof RedstoneWallTorchBlock) {
            return state.getLightEmission();
        }
        return WorldLightMath.calculateLightByHeight(state, pos.getY());
    }
}

@Mixin(LanternBlock.class)
class LanternLightMixin {

    @Unique
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    @Unique
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return WorldLightMath.calculateLightByHeight(state, pos.getY());
    }
}

@Mixin(BaseFireBlock.class)
class BaseFireMixin {

    @Unique
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    @Unique
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return WorldLightMath.calculateLightByHeight(state, pos.getY(), 2);
    }
}

@Mixin(CampfireBlock.class)
class CampfireLightMixin {

    @Unique
    public boolean hasDynamicLightEmission(BlockState state) {
        return true;
    }

    @Unique
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        boolean isLit = CampfireBlock.isLitCampfire(state);
        return isLit ? WorldLightMath.calculateLightByHeight(state, pos.getY(),2) : 0;
    }

    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void forceUnlitOnPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        BlockState originalState = cir.getReturnValue();
        if (originalState != null) {
            if (context.getClickedPos().getY() < 0) {
                cir.setReturnValue(originalState.setValue(CampfireBlock.LIT, false));
            }
        }
    }
}

