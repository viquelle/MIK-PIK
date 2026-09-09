package com.viquelle.mikpik.item.items;

import com.viquelle.mikpik.registry.ModDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

@EventBusSubscriber(modid = "mikpik")
public class HamBatItem extends Item {
    public HamBatItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.has(ModDataComponents.SPOIL_TIME.get());
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int spoilTime = stack.getOrDefault(ModDataComponents.SPOIL_TIME.get(), 1000);
        float timeRemaining = stack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING.get(), (float) spoilTime);
        float ratio = timeRemaining / spoilTime;
        return Math.round(13.0F * ratio);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int spoilTime = stack.getOrDefault(ModDataComponents.SPOIL_TIME.get(), 1000);
        float timeRemaining = stack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING.get(), (float) spoilTime);
        float ratio = timeRemaining / spoilTime;

        if (ratio >= 0.66F) return 0x00FF00;
        if (ratio >= 0.33F) return 0xFFFF00;
        return 0xFF0000;
    }

    @SubscribeEvent
    public static void onAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof HamBatItem)) return;

        int spoilTime = stack.getOrDefault(ModDataComponents.SPOIL_TIME.get(), 1000);
        float timeRemaining = stack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING.get(), (float) spoilTime);
        float ratio = timeRemaining / spoilTime;

        float rawDamage = 5.0F + (ratio * 3F);
        float attackSpeed = 0.9F;

        event.replaceModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath("mikpik", "hat_bat_damage"),
                rawDamage,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.MAINHAND);

        event.replaceModifier(Attributes.ATTACK_SPEED, new AttributeModifier(
                ResourceLocation.fromNamespaceAndPath("mikpik", "hat_bat_speed"),
                attackSpeed - 4.0F,
                AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.MAINHAND);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        double dx = attacker.getX() - target.getX();
        double dz = attacker.getZ() - target.getZ();
        target.knockback(0.5F, dx, dz);
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}