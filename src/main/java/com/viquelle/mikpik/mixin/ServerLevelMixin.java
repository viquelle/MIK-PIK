package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.sleep.SleepManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Redirect(
            method = "tickTime",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/SleepStatus;areEnoughSleeping(I)Z"
            )
    )
    private boolean mikpik$checkEnoughSleeping(SleepStatus sleepStatus, int percentage) {
        ServerLevel level = (ServerLevel)(Object)this;

        return SleepManager.areEnoughPlayersSleeping(
                level.getServer(),
                percentage
        );
    }

    @Redirect(
            method = "tickTime",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/SleepStatus;areEnoughDeepSleeping(ILjava/util/List;)Z"
            )
    )
    private boolean mikpik$checkEnoughDeepSleeping(SleepStatus sleepStatus, int percentage, List<ServerPlayer> players) {
        ServerLevel level = (ServerLevel)(Object)this;

        return SleepManager.areEnoughPlayersSleeping(
                level.getServer(),
                percentage
        );
    }


    @Inject(
            method = "updateSleepingPlayerList",
            at = @At("HEAD"),
            cancellable = true
    )
    private void mikpik$updateGlobalSleepStatus(CallbackInfo ci) {
        ServerLevel level = (ServerLevel)(Object)this;

        if (level == level.getServer().overworld()) {
            SleepManager.announceGlobalSleepStatus(level.getServer());
            ci.cancel();
        }
    }
}