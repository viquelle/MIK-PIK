package com.viquelle.mikpik.ghost;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.item.items.HeartItem;
import com.viquelle.mikpik.registry.ModItems;
import com.viquelle.mikpik.network.payload.GhostStatePayload;
import com.viquelle.mikpik.network.payload.HeartReviveRequestPayload;
import com.viquelle.mikpik.network.payload.PushItemPayload;
import com.viquelle.mikpik.registry.ModAttachments;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;


@EventBusSubscriber(modid = MikpikMod.MODID)
public class GhostManager {
    public static boolean isGhost(Player player) {
        return player.getData(ModAttachments.IS_GHOST);
    }

    public static void revive(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new GhostStatePayload(false));
        }
        player.setData(ModAttachments.IS_GHOST, false);
        updateAbilities(player, false);
    }

    public static void becomeGhost(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new GhostStatePayload(true));
        }
        player.setData(ModAttachments.IS_GHOST, true);
        updateAbilities(player, true);
    }

    public static void updateAbilities(Player player, boolean isGhost) {
        player.setInvulnerable(isGhost);
        player.getAbilities().invulnerable = isGhost;
        player.getAbilities().flying = isGhost;
        player.getAbilities().mayfly = isGhost;
        player.getAbilities().mayBuild = !isGhost;
        player.onUpdateAbilities();
        player.minorHorizontalCollision = !isGhost;
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20);

    }

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewAboutToBeSetTarget();

        if (target instanceof Player player && GhostManager.isGhost(player)) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        event.setCanceled(true);
        if (GhostManager.isGhost(player)) return;

        MinecraftServer server = player.getServer();
        if (server != null) {
            server.getPlayerList().broadcastSystemMessage(
                    event.getSource().getLocalizedDeathMessage(player),
                    false
            );
        }

        player.level().getScoreboard().forAllObjectives(ObjectiveCriteria.DEATH_COUNT, player, ScoreAccess::increment);
        player.awardStat(Stats.DEATHS);
        player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_DEATH));
        player.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        player.clearFire();
        player.setTicksFrozen(0);
        player.setSharedFlagOnFire(false);

        player.setLastDeathLocation(Optional.of(GlobalPos.of(player.level().dimension(), player.blockPosition())));
        player.stopRiding();
        player.stopUsingItem();
        player.stopSleeping();

        player.setHealth(10f);
        if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            player.getInventory().dropAll();
            player.drop(player.containerMenu.getCarried(), false);
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }

        player.setArrowCount(0);
        player.removeAllEffects();
        GhostManager.becomeGhost(player);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player))
            return;

        if (GhostManager.isGhost(player)) {
            GhostManager.updateAbilities(player, true);

            PacketDistributor.sendToPlayer(
                    player,
                    new GhostStatePayload(true)
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!GhostManager.isGhost(player)) return;

        player.setSprinting(false);
        player.removeAllEffects();
        Vec3 delta = player.getDeltaMovement();
        player.setDeltaMovement(
                delta.x * 0.7,
                delta.y * 0.5,
                delta.z * 0.7
        );
        player.walkAnimation.setSpeed(player.walkAnimation.speed() * 0.7f);
    }

    @SubscribeEvent
    public static void OnVanillaRevive(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            revive(player);
        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (isGhost(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onGhostInteract(PlayerInteractEvent.EntityInteract event) {
        Player issuer = event.getEntity();
        if (issuer.level().isClientSide) return;
        if (!(event.getTarget() instanceof Player target)) return;
        if (!GhostManager.isGhost(target)) return;

        ItemStack heart = issuer.getMainHandItem();
        if (!(heart.is(ModItems.HEART.get())) && !HeartItem.isCharged(heart)) return;

        GhostManager.revive(target);
        heart.shrink(1);
        event.setCanceled(true);
    }


    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

//    @SubscribeEvent
//    public static void onRightClickBlock(PlayerInteractEvent.RightClickItem event) {
//        if (isGhost(event.getEntity())) {
//            event.setCanceled(true);
//        }
//    }

    @SubscribeEvent
    public static void onPickup(ItemEntityPickupEvent.Pre event) {
        if (isGhost(event.getPlayer())) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player && isGhost(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (isGhost(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() || !isGhost(player)) return;
        PacketDistributor.sendToServer(PushItemPayload.INSTANCE);
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() || !isGhost(player)) return;
        PacketDistributor.sendToServer(PushItemPayload.INSTANCE);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && isGhost(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isGhost(event.getEntity())) {
            tryHeartRaycastRevive(event.getEntity());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isGhost(event.getEntity())) {
            tryHeartRaycastRevive(event.getEntity());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();

        if (!player.level().isClientSide || !GhostManager.isGhost(player)) return;

        HitResult hit = raycast(player, 4.0);

        if (hit instanceof EntityHitResult result) {
            if (result.getEntity() instanceof ItemEntity entity) {
                if (entity.getItem().is(ModItems.HEART.get())) {
                    MikpikMod.LOGGER.debug("Client: sending revive request");
                    PacketDistributor.sendToServer(HeartReviveRequestPayload.INSTANCE);
                }
            }
        }
    }

    public static HitResult raycast(Player player, double range) {
        Vec3 start = player.getEyePosition();
        Vec3 direction = player.getViewVector(1.0f);
        Vec3 end = start.add(direction.scale(range));

        AABB searchArea = player.getBoundingBox()
                .expandTowards(direction.scale(range))
                .inflate(1.0);

        HitResult result = ProjectileUtil.getEntityHitResult(
                player,
                start,
                end,
                searchArea,
                entity -> entity instanceof ItemEntity,
                range * range
        );
        return result;
    }

    public static boolean tryHeartRaycastRevive(Player player) {
        if (player.level().isClientSide) return false;

        if (!GhostManager.isGhost(player)) return false;

        HitResult hit = raycast(player, player.blockInteractionRange());

        if (hit instanceof EntityHitResult entityHitResult) {
            if (entityHitResult.getEntity() instanceof ItemEntity itemEntity) {
                if (itemEntity.getItem().is(ModItems.HEART.get())) {

                    player.level().playSound(
                            null,
                            player.getX(), player.getY(),player.getZ(),
                            SoundEvents.SOUL_ESCAPE,
                            SoundSource.BLOCKS,
                            1F,
                            1F
                    );

                    GhostManager.revive(player);
                    itemEntity.discard();

                    return true;
                }
            }
        }
        return false;
    }
}
