package com.viquelle.examplemod.item.lightItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import java.util.List;

public abstract class AbstractLightItem extends Item {
    protected final List<CurveSegment> curves;
    protected final float fuelDrainPerTick;
    public AbstractLightItem(Properties properties,
                             List<CurveSegment> curves,
                             float fuelDrainPerTick) {
        super(properties);
        this.curves = curves;
        this.fuelDrainPerTick = fuelDrainPerTick;
    }

    public static boolean isEnabled(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("enabled");
    }

    public static void setEnabled(ItemStack stack, boolean value) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag(); tag.putBoolean("enabled",value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static int getTicks(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("ticks");
    }

    public static void setTicks(ItemStack stack, int value) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag(); tag.putInt("ticks", value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void setFuel(ItemStack stack, float value) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var tag = data.copyTag(); tag.putFloat("fuel", value);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static float getFuel(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getFloat("fuel");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            boolean enabled = isEnabled(stack);
            setEnabled(stack, !enabled);

            if (!enabled) {
                setTicks(stack, 0);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide && !selected && !isEnabled(stack)) return;

        float fuel = getFuel(stack);
        if (fuel <= 0) {
            setEnabled(stack,false);
            return;
        }

        setFuel(stack, fuel - fuelDrainPerTick);
        setTicks(stack, getTicks(stack) + 1);
    }

    public float computeBrightness(ItemStack stack) {
        int t = getTicks(stack);

        for (CurveSegment seg : curves) {
            if (t >= seg.startTick() && t <= seg.endTick()) {
                float x = (float)(t - seg.startTick()) / (float)(seg.endTick() - seg.startTick());

                return lerp(seg.from(), seg.to(), applyCurve(x, seg.curve()));
            }
        }
        return 0f;
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private float applyCurve(float x, LightCurve c) {
        return switch (c) {
            case LINEAR -> x;
            case EASE_IN -> x * x;
            case EASE_OUT -> 1 - (1 - x) * (1 - x);
            case EASE_IN_OUT -> x < 0.5f ? 2 * x * x : 1 - (float) Math.pow(-2 * x + 2, 2) / 2;
            case FLICKER -> x + ((float) Math.random() - 0.5f) * 0.1f;
        };
    }
}
