package com.viquelle.examplemod.item;

import com.viquelle.examplemod.client.light.LightCurve;
import com.viquelle.examplemod.client.light.PointLight;
import net.minecraft.world.entity.player.Player;

public class LighterItem extends AbstractLightItem {
    public static final String ITEM_NAME = "lighter";

    public LighterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public String getKey(Player player) {
        return player.getUUID() + "_" + ITEM_NAME;
    }

    @Override
    public PointLight createLight(Player player) {
        return new PointLight.Builder(player)
                .setColor(0xFFAA33) // Теплый оранжевый
                .setBrightness(0.0f, 0.8f) // Мягкий свет
                .setRadius(7f)
                // Зажигается за 0.3с, гаснет плавно за 0.5с
                .setSpeeds(0.3f, 0.5f)
                .setCurves(LightCurve.EASE_OUT, LightCurve.EASE_IN)
                .build();
    }
}