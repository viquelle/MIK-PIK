package com.viquelle.examplemod.item;

import com.viquelle.examplemod.item.lightItems.AbstractLightItem;
import com.viquelle.examplemod.item.lightItems.CurveSegment;
import com.viquelle.examplemod.item.lightItems.LightCurve;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

public class FlashlightItem extends AbstractLightItem {
    private boolean isOn = false;
    public FlashlightItem(Properties properties) {
        super(
                properties,
                List.of(
                        new CurveSegment(0,12,0f,0.5f, LightCurve.EASE_IN),
                        new CurveSegment(13, 16, 0.5f, 0.4f, LightCurve.FLICKER),
                        new CurveSegment(17,20,0.4f, 1f, LightCurve.EASE_OUT)
                ),
                0.002f);
    }

//    public FlashlightItem(Item.Properties props){
//        super(props.stacksTo(1));
//    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
        ItemStack stack = player.getItemInHand(hand);

        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);

        var tag = data.copyTag();

        boolean enabled = tag.getBoolean("enabled");

        enabled = !enabled;
        tag.putBoolean("enabled", enabled);

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (!level.isClientSide) {
            isOn = !isOn;
            String message = isOn ? "ВКЛ" : "ВЫКЛ";
            player.displayClientMessage(Component.literal(enabled ? "вкл" : "выкл"), true);
        }

        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
