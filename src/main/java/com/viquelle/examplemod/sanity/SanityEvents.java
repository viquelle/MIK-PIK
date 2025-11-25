package com.viquelle.examplemod.sanity;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import static com.viquelle.examplemod.ExampleMod.LOGGER;

public class SanityEvents {
    public static void register() {
        NeoForge.EVENT_BUS.register(new SanityEvents());
    }

    @SubscribeEvent
    public void onLoading(PlayerEvent.PlayerLoggedInEvent event) {
        SanityUtil.sync(event.getEntity());
        LOGGER.info("SYNCING");
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onLivingHurt(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player player) {
            DamageSource source = event.getSource();
            float damage = event.getNewDamage();
            SanityUtil.reduce(player, damage);
            player.sendSystemMessage(Component.literal("Damage: "+ damage + "Sanity: " + SanityUtil.getSanity(player)));
        }
    }

    @SubscribeEvent
    public void onLivingTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            int light = player.level().getMaxLocalRawBrightness(player.blockPosition());
            //LOGGER.info("[SANITYEVENTS] [LIGHT] {}",light);
            if (light == 0) {
                SanityUtil.reduce(player,0.1f);
            } else {
                SanityUtil.add(player, 0.1f);
            }
            
        }
    }
}
