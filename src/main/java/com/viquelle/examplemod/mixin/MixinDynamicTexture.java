package com.viquelle.examplemod.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.viquelle.examplemod.darknesscomputer.Darkness;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DynamicTexture.class)
public class MixinDynamicTexture{
    @Shadow
    NativeImage pixels;

    @Inject(method = "upload", at = @At(value = "HEAD"))
    private void onUpload(CallbackInfo ci) {
        if (pixels != null && Darkness.enabled) {
            final NativeImage img = pixels;

            for (int b = 0; b < 16; b++) {
                for (int s = 0; s < 16; s++) {
                    final int color = Darkness.darken(img.getPixelRGBA(b,s),b,s);
                    img.setPixelRGBA(b,s,color);
                }
            }
        }
    }
}
