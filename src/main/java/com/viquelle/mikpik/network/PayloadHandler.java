package com.viquelle.mikpik.network;

import com.viquelle.mikpik.ICampfireFuel;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.blockentity.MeatEffigyBlockEntity;
import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.ghost.HealthPenailtyUtil;
import com.viquelle.mikpik.light.ServerLightManager;
import com.viquelle.mikpik.mixin.CampfireBlockEntityMixin;
import com.viquelle.mikpik.network.payload.*;
import com.viquelle.mikpik.sanity.ClientSanityData;
import com.viquelle.mikpik.util.CampfireCookingHelper;
import com.viquelle.mikpik.world.Gameplay;
import com.viquelle.mikpik.world.GameplayMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.jar.Attributes;

import static com.viquelle.mikpik.ghost.GhostManager.raycast;
import static com.viquelle.mikpik.ghost.GhostManager.tryHeartRaycastRevive;

public class PayloadHandler {
    public static void handleDataOnNetwork(final SanitySyncPayload data, final IPayloadContext context) {
        ClientSanityData.set(data.sanity());
    }

    public static void handleDynamicBright(final DynamicBrightPayload data, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (!player.level().isClientSide) {
                ServerLightManager.setPlayerDynamicBright(player.getUUID(), data.bright());
            }
        });
    }

    public static void handleHeartReviveRequest(HeartReviveRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (tryHeartRaycastRevive(player)) {
                MikpikMod.LOGGER.info("{} revived, send packet to client", player.getName());
                PacketDistributor.sendToPlayer(player, ReviveSuccessPayload.INSTANCE);
            }
        });
    }

    public static void handleGhostRespawnRequest(GhostRespawnRequest ghostRespawnRequest, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            if (!GhostManager.isGhost(player)) return;

            MeatEffigyBlockEntity entity = MeatEffigyBlockEntity.getValidEffigy(player);
            if (entity != null) {
                MeatEffigyBlockEntity.tryReviveAtEffigy(player);
                return;
            }

            if (Gameplay.isDst(player.level())) return;
            // Дальше дефолтное возрождение
            DimensionTransition transition = player.findRespawnPositionAndUseSpawnBlock(
                    false,
                    DimensionTransition.DO_NOTHING
            );
            Vec3 pos = transition.pos();
            GhostManager.revive(player);
            HealthPenailtyUtil.addPenaltySoft(player, 0.25);

            player.teleportTo(
                    transition.newLevel(),
                    pos.x,
                    pos.y,
                    pos.z,
                    0,
                    0
            );
        });
    }

    public static void handleReviveResult(ReviveSuccessPayload payload, IPayloadContext context) {
        MikpikMod.LOGGER.info("revive result gotcha");
    }

    public static void handePushRequest(PushItemPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (!GhostManager.isGhost(player)) return;;

                HitResult hit = raycast(player, 4.0);

                if (hit instanceof EntityHitResult result) {
                    if (result.getEntity() instanceof ItemEntity item) {
                        Vec3 direction = player.getLookAngle().normalize();
                        double pushStrength = 0.8;

                        item.addDeltaMovement(
                                direction.scale(pushStrength)
                        );
                        item.hurtMarked = true;
                    }
                }
            }
        });
    }

    public static void handleGhostState(GhostStatePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;

            if (player == null) return;

            if (payload.isGhost()) {
                GhostManager.becomeGhost(player);
            } else {
                GhostManager.revive(player);
            }
        });
    }

    public static void handleGameplayMode(GameplayModePayload payload, IPayloadContext context) {
        context.enqueueWork(() ->
                Gameplay.setClientMode(payload.mode())
        );
    }

    public static void handleCookRequest(CampfireCookRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) context.player();
            var level = player.level();
            var pos = payload.pos();

            if (!(level.getBlockEntity(pos) instanceof ICampfireFuel beMixin)) return;
            if (beMixin.mikpik$getFuelTime() <= 0) return;

            ItemStack handItem = player.getMainHandItem();
            var recipe = CampfireCookingHelper.getCookingRecipe(level, handItem);
            if (recipe == null) return;

            ItemStack result = CampfireCookingHelper.processCookedItem(
                    handItem,
                    recipe.value().getResultItem(level.registryAccess()),
                    level.registryAccess()
            );
            handItem.shrink(1);

            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
        });
    }

}
