package com.viquelle.mikpik.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.viquelle.mikpik.TextureAccess;
import com.viquelle.mikpik.darknesscomputer.Darkness;
import net.minecraft.client.renderer.texture.DynamicTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.viquelle.mikpik.darknesscomputer.Darkness.enabled;

@Mixin(DynamicTexture.class)
public class MixinDynamicTexture implements TextureAccess {
    @Shadow
    NativeImage pixels;

    private boolean enableHook = false;

    @Inject(method = "upload", at = @At(value = "HEAD"))
    private void onUpload(CallbackInfo ci) {
        if (enableHook && pixels != null && enabled) {
            final NativeImage img = pixels;

            for (int b = 0; b < 16; b++) {
                for (int s = 0; s < 16; s++) {
                    final int color = Darkness.darken(img.getPixelRGBA(b,s),b,s);
                    img.setPixelRGBA(b,s,color);
                }
            }
        }
    }

    @Override
    public void enableUploadHook() {
        enableHook = true;
    }
}
