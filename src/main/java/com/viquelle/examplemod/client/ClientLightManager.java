package com.viquelle.examplemod.client;

import com.viquelle.examplemod.ExampleMod;
import com.viquelle.examplemod.client.light.AbstractLight;
import com.viquelle.examplemod.client.light.PointLight;
import com.viquelle.examplemod.datagen.ModConfig;
import com.viquelle.examplemod.item.AbstractLightItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import java.util.*;


@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public class ClientLightManager {
    private static final Map<String, AbstractLight<?>> activeLights = new HashMap<>();
    private static final String AMBIENT_SUFFIX = "_ambient";

    private static long lastFrameTime = System.currentTimeMillis();
    private static final Set<String> currentFrameKeys = new HashSet<>();

    private static int environmentTickCounter = 0;
    private static boolean cachedIsDark = false;

    public static void initPlayerAmbientLight(LocalPlayer player) {
        String key = player.getUUID() + AMBIENT_SUFFIX;
        if (activeLights.containsKey(key)) return;
        float configBrightness = ModConfig.AMBIENT_BRIGHTNESS.get().floatValue();

        PointLight light = new PointLight.Builder(player)
                .setBrightness(0f, configBrightness)
                .setSpeeds(3.0f, 1.0f)
                .setColor(0xAAAAFF)
                .build();

        light.setPermanent(true);
        add(key, light);
    }

    @SubscribeEvent
    public static void onLogin(ClientPlayerNetworkEvent.LoggingIn e) {
        Minecraft.getInstance().execute(() -> {
            ExampleMod.LOGGER.info("Client TICK");
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                ClientLightManager.initPlayerAmbientLight(mc.player);
            }
        });
    }

    public static void add(String key, AbstractLight<?> light) {
        activeLights.put(key, light);
        light.register();
    }

    public static void tick(float partialTick) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) return;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastFrameTime) / 1000f;
        lastFrameTime = currentTime;

        currentFrameKeys.clear();

        for (Player player : localPlayer.level().players()) {
            checkPlayerEquipment(player);
        }

        updateAmbientKey(localPlayer);

        Iterator<Map.Entry<String, AbstractLight<?>>> it = activeLights.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, AbstractLight<?>> entry = it.next();
            String key = entry.getKey();
            AbstractLight<?> light = entry.getValue();

            if (currentFrameKeys.contains(key)) {
                light.turnOn();
            } else {
                light.turnOff();
            }

            light.tick(deltaTime, partialTick);

            if (light.isDead()) {
                light.unregister();
                it.remove();
            }
        }
    }

    private static void updateAmbientKey(LocalPlayer player) {
        String ambientKey = player.getUUID() + AMBIENT_SUFFIX;
        AbstractLight<?> ambient = activeLights.get(ambientKey);

        if (environmentTickCounter++ % 10 == 0) {
            BlockPos pos = player.blockPosition();
            int skyLight = player.level().getBrightness(LightLayer.SKY, pos);
            int blockLight = player.level().getBrightness(LightLayer.BLOCK, pos);
            cachedIsDark = (skyLight < 2 && blockLight == 0);
        }

        boolean hasOtherLight = false;
        String playerUUID = player.getUUID().toString();
        for (String activeKey : currentFrameKeys) {
            if (activeKey.startsWith(playerUUID) && !activeKey.equals(ambientKey)) {
                hasOtherLight = true;
                break;
            }
        }

        if (!hasOtherLight && cachedIsDark) {
            if (ambient == null) { initPlayerAmbientLight(player); }
            currentFrameKeys.add(player.getUUID() + AMBIENT_SUFFIX);
        }
    }

    private static void checkPlayerEquipment(Player player) {
        checkHand(player, InteractionHand.MAIN_HAND);
        checkHand(player, InteractionHand.OFF_HAND);
    }

    private static void checkHand(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof AbstractLightItem item) {
            if (AbstractLightItem.isEnabled(stack)) {
                String key = item.getKey(player);
                currentFrameKeys.add(key);

                if (!activeLights.containsKey(key)) {
                    add(key, item.createLight(player));
                }
            }
        }
    }
}