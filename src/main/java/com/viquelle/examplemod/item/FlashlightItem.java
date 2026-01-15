package com.viquelle.examplemod.item;

import com.viquelle.examplemod.client.light.AreaLight;
import com.viquelle.examplemod.client.light.LightCurve;
import com.viquelle.examplemod.client.light.modifiers.FlickerModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

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
                .setBrightness(0.0f, 2.5f)
                .setSpeeds(0.2f,0.1f)
                .setCurves(LightCurve.EASE_IN, LightCurve.EASE_OUT)
                .setAngle(0.6f)
                .setDistance(16f)
                .build();
        light.addModifier(new FlickerModifier(3.0f, 0.03f, 0.05f));
        return light;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack,context,tooltipComponents,tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.examplemod.flashlight.desc").withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC));
    }
}