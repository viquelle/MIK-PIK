package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.ICampfireFuel;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.util.CampfireCookingHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(CampfireBlock.class)
public class CampfireBlockMixin {
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void mikpik$forceInitialState(net.minecraft.world.item.context.BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        MikpikMod.LOGGER.info("getStatePushed");
        BlockState state = cir.getReturnValue();
        if (state.getValue(CampfireBlock.LIT) && CampfireCookingHelper.INITIAL_FUEL_TIME <= 0) {
            cir.setReturnValue(state.setValue(CampfireBlock.LIT, false));
        }
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void mikpik$handleRightClick(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
        if (level.isClientSide) {
            if (CampfireCookingHelper.getFuelValue(stack) > 0) {
                double centerX = pos.getX() + 0.5;
                double centerY = pos.getY() + 0.5;
                double centerZ = pos.getZ() + 0.5;

                for (int i = 0; i < 10; i++) {
                    double offsetX = (level.random.nextDouble() - 0.5) * 0.8;
                    double offsetY = level.random.nextDouble() * 0.6;
                    double offsetZ = (level.random.nextDouble() - 0.5) * 0.8;

                    double velX = (level.random.nextDouble() - 0.5) * 0.15;
                    double velY = level.random.nextDouble() * 0.25 + 0.1;
                    double velZ = (level.random.nextDouble() - 0.5) * 0.15;

                    level.addParticle(ParticleTypes.FLAME, centerX + offsetX, centerY + offsetY, centerZ + offsetZ, velX, velY, velZ);

                    if (level.random.nextBoolean()) {
                        level.addParticle(ParticleTypes.SMALL_FLAME, centerX + offsetX, centerY + offsetY, centerZ + offsetZ, velX, velY, velZ);
                    }
                }
            }
            cir.setReturnValue(ItemInteractionResult.CONSUME);
            return;
        }

        if (level.getBlockEntity(pos) instanceof ICampfireFuel beMixin) {
            int fuelValue = CampfireCookingHelper.getFuelValue(stack);
            MikpikMod.LOGGER.info("{} || {}", stack, fuelValue);
            if (fuelValue > 0) {
                if (!state.getValue(CampfireBlock.LIT)) {
                    level.setBlock(pos, state.setValue(CampfireBlock.LIT, true), 3);

                }
                beMixin.mikpik$addFuel(fuelValue);
                stack.shrink(1);

                float pitch = 0.8F + level.random.nextFloat() * 0.2F;
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.4F, pitch);

                cir.setReturnValue(ItemInteractionResult.SUCCESS);
                return;
            }
            if (CampfireCookingHelper.getCookingRecipe(level, stack) != null) {
                cir.setReturnValue(ItemInteractionResult.CONSUME);
            }
        }
    }

    @Inject(method = "getTicker", at = @At("HEAD"), cancellable = true)
    private <T extends BlockEntity> void mikpik$overrideTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType, CallbackInfoReturnable<BlockEntityTicker<T>> cir) {
        if (level.isClientSide) {
            if (state.getValue(CampfireBlock.LIT)) {
                cir.setReturnValue(((level1, blockPos, blockState, t) -> {
                    CampfireBlockEntity.particleTick(level1, blockPos, blockState, (CampfireBlockEntity) t);
                }));
            }
        } else {
            if (state.getValue(CampfireBlock.LIT)) {
                cir.setReturnValue(((level1, blockPos, blockState, t) -> {
                    if (t instanceof ICampfireFuel be) {
                        if (be.mikpik$getFuelTime() > 0) {
                            be.mikpik$addFuel(-1);
                            be.mikpik$setLastUpdate(level1.getGameTime());
                            level.sendBlockUpdated(blockPos, blockState, blockState, 3);
                        } else {
                            level.levelEvent(null, 1009, blockPos, 0);
                            BlockState unlitState = blockState.setValue(CampfireBlock.LIT, false);
                            level1.setBlock(blockPos, unlitState, 3);
                        }
                    }
                }));
            }
        }
    }
}