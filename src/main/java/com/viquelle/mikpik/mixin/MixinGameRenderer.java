package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.LightMapAccess;
import com.viquelle.mikpik.darknesscomputer.Darkness;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {
    @Shadow
    private Minecraft minecraft;
    @Shadow
    private LightTexture lightTexture;

    @Shadow public abstract LightTexture lightTexture();

    @Inject(method = "renderLevel", at = @At(value = "HEAD"))
    private void onRenderLevel(DeltaTracker deltaTracker, CallbackInfo ci){
        final LightMapAccess lightMapAccess = (LightMapAccess) lightTexture;
        if (lightMapAccess.isDirty()) {
            minecraft.getProfiler().push("lightTex");
            Darkness.updateLuminance(minecraft, (GameRenderer) (Object) this, deltaTracker.getGameTimeDeltaTicks(), lightMapAccess.prevFlicker());
            minecraft.getProfiler().pop();
        }
    }
}
