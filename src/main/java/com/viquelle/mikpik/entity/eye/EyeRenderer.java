package com.viquelle.mikpik.entity.eye;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.viquelle.mikpik.MikpikMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class EyeRenderer extends EntityRenderer<EyeEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "textures/entity/eye.png");

    public EyeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EyeEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(
            EyeEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        poseStack.pushPose();
        poseStack.translate(0.0f, entity.getBbHeight()/2.0f, 0.0f);
        rotateToPlayer(entity, partialTick, poseStack);

        renderEyeQuad(poseStack, bufferSource);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static void rotateToPlayer(EyeEntity entity, float partialTick, PoseStack poseStack) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        Vec3 from = entity.position().add(0.0f, entity.getBbHeight()/2.0f, 0.0f);
        Vec3 to = player.getEyePosition(partialTick);

        Vec3 dir = to.subtract(from).normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));
        float pitch = (float) -Math.toDegrees(Math.asin(dir.y));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
    }

    private static void renderEyeQuad(PoseStack poseStack, MultiBufferSource bufferSource) {


        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        int light = LightTexture.FULL_BRIGHT;
        int overlay = OverlayTexture.NO_OVERLAY;

        float hw = EyeEntity.WIDTH / 2.0F;
        float hh = EyeEntity.HEIGHT / 2.0F;

        vertex(consumer, matrix, -hw, -hh, 0.0F, 0.0F, 1.0F, light, overlay);
        vertex(consumer, matrix,  hw, -hh, 0.0F, 1.0F, 1.0F, light, overlay);
        vertex(consumer, matrix,  hw,  hh, 0.0F, 1.0F, 0.0F, light, overlay);
        vertex(consumer, matrix, -hw,  hh, 0.0F, 0.0F, 0.0F, light, overlay);

//        vertex(consumer, matrix, -0.25f, -0.75f, 0.0F, 0.0F, 1.0F, light, overlay);
//        vertex(consumer, matrix, 0.75f, -0.75f, 0.0F, 1.0F, 1.0F, light, overlay);
//        vertex(consumer, matrix, 0.75f, 0.25f, 0.0F, 1.0F, 0.0F, light, overlay);
//        vertex(consumer, matrix, -0.25f,  0.25f, 0.0F, 0.0F, 0.0F, light, overlay);
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            float x,
            float y,
            float z,
            float u,
            float v,
            int light,
            int overlay
    ) {
        consumer.addVertex(matrix, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(0.0F, 1.0F, 0.0F);
    }
}