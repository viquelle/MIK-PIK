package com.viquelle.examplemod.item;

import com.viquelle.examplemod.client.light.AreaLight;
import com.viquelle.examplemod.client.light.LightCurve;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FlashlightItem extends AbstractLightItem {
    public static final String ITEM_NAME = "flashlight";

    public FlashlightItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public String getKey(Player player) {
        return player.getUUID() + "_" + ITEM_NAME;
    }

    @Override
    public AreaLight createLight(Player player) {
        AreaLight light = new AreaLight.Builder(player)
                .setColor(0xAAAAFF)
                .setBrightness(0.0f, 1.2f)
                .setSpeeds(0.2f,0.1f)
                .setCurves(LightCurve.EASE_IN, LightCurve.EASE_IN)
                .setAngle(0.6f)
                .setDistance(32f)
                .build();
        return light;
    }
}