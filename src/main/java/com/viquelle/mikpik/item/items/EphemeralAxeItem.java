package com.viquelle.mikpik.item.items;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.registry.ModItems;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class EphemeralAxeItem extends AxeItem {
    public static final Tier EPHEMERAL_TIER = new SimpleTier(
            BlockTags.INCORRECT_FOR_GOLD_TOOL,
            0,
            16.0F,
            0.0F,
            0,
            () -> Ingredient.EMPTY
    );

    public EphemeralAxeItem(Item.Properties properties) {
        super(EPHEMERAL_TIER, properties.rarity(Rarity.EPIC));
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @SubscribeEvent
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        event.modify(
                ModItems.EPHEMERAL_AXE.get(),
                builder -> {
                    builder.remove(DataComponents.MAX_DAMAGE);
                    builder.remove(DataComponents.DAMAGE);
                }
        );
    }
}
