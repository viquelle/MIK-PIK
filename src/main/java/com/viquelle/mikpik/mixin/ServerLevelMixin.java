package com.viquelle.mikpik.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.viquelle.mikpik.sleep.DreamDimension;
import com.viquelle.mikpik.sleep.DreamManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Unique
    private long mikpik$dreamDayTime;

    @ModifyExpressionValue(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/SleepStatus;areEnoughDeepSleeping(ILjava/util/List;)Z"
            )
    )
    private boolean mikpik$handleSleep(boolean enoughDeepSleeping) {
        ServerLevel level = (ServerLevel) (Object) this;

        if (enoughDeepSleeping) {
            List<ServerPlayer> sleepingPlayers = level.players()
                    .stream()
                    .filter(ServerPlayer::isSleeping)
                    .toList();

            for (ServerPlayer player : sleepingPlayers) {
                DreamManager.enterDream(player);
            }

            level.setDayTimePerTick(5.0F);
        }

        return false;
    }
}