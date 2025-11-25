package com.viquelle.examplemod.mixin;

import com.viquelle.examplemod.LightMapAccess;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightTexture.class)
public class MixinLightTexture implements LightMapAccess {
    @Shadow
    private DynamicTexture lightTexture;
    @Shadow
    private boolean updateLightTexture;
    @Shadow
    private float blockLightRedFlicker;

    @Override
    public boolean isDirty() {
        return updateLightTexture;
    }

    @Override
    public float prevFlicker() {
        return blockLightRedFlicker;
    }
}
