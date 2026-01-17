package com.viquelle.mikpik.datagen;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    private final String locale;

    public ModLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        if (locale.equals("ru_ru")) {
            add_ru();
        } else {
            add_en();
        }
    }

    private void add_en() {
        add("itemGroup." + MikpikMod.MODID, "MIK PIK");

        // Предметы
        addItem(ModItems.LIGHTER, "Lighter");
        addItem(ModItems.FLASHLIGHT, "Flashlight");

        // Тултипы из AbstractLightItem
        add("tooltip.mikpik.status", "Status: ");
        add("tooltip.mikpik.status.on", "ON");
        add("tooltip.mikpik.status.off", "OFF");

        // Тултипы конкретных предметов
        add("tooltip.mikpik.lighter.desc", "1");
        add("tooltip.mikpik.flashlight.desc", "2");
    }

    private void add_ru() {
        add("itemGroup." + MikpikMod.MODID, "МЫК ПЫК");

        // Предметы
        addItem(ModItems.LIGHTER, "Зажигалка");
        addItem(ModItems.FLASHLIGHT, "Фонарик");

        // Тултипы из AbstractLightItem
        add("tooltip.mikpik.status", "Состояние: ");
        add("tooltip.mikpik.status.on", "ВКЛ");
        add("tooltip.mikpik.status.off", "ВЫКЛ");

        // Тултипы конкретных предметов
        add("tooltip.mikpik.lighter.desc", "1");
        add("tooltip.mikpik.flashlight.desc", "2");
    }
}
