package com.viquelle.examplemod.item;

import com.viquelle.examplemod.client.light.LightCurve;
import com.viquelle.examplemod.client.light.PointLight;
import com.viquelle.examplemod.client.light.modifiers.FlickerModifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

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
        PointLight l = new PointLight.Builder(player)
                .setColor(0xFFAA33)
                .setBrightness(0.0f, 0.8f)
                .setRadius(7f)
                .setSpeeds(0.3f, 0.5f)
                .setCurves(LightCurve.EASE_OUT, LightCurve.EASE_IN)
                .build();
        l.addModifier(new FlickerModifier(3.0f, 0.08f, 0.02f));
        return l;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack,context,tooltipComponents,tooltipFlag);
        tooltipComponents.add(Component.translatable("tooltip.examplemod.lighter.desc").withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC));
    }
}