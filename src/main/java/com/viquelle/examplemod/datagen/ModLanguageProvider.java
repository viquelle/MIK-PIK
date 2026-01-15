package com.viquelle.examplemod.datagen;

import com.ibm.icu.util.ULocale;
import com.viquelle.examplemod.ExampleMod;
import com.viquelle.examplemod.item.FlashlightItem;
import com.viquelle.examplemod.item.LighterItem;
import com.viquelle.examplemod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    private final String locale; // Сохраняем локаль, чтобы проверять её

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
        add("itemGroup." + ExampleMod.MODID, "Example Mod");

        // Предметы
        addItem(ModItems.LIGHTER, "Lighter");
        addItem(ModItems.FLASHLIGHT, "Flashlight");

        // Тултипы из AbstractLightItem
        add("tooltip.examplemod.status", "Status: ");
        add("tooltip.examplemod.status.on", "ON");
        add("tooltip.examplemod.status.off", "OFF");

        // Тултипы конкретных предметов
        add("tooltip.examplemod.lighter.desc", "1");
        add("tooltip.examplemod.flashlight.desc", "2");
    }

    private void add_ru() {
        add("itemGroup." + ExampleMod.MODID, "bob bob skebob");

        // Предметы
        addItem(ModItems.LIGHTER, "Зажигалка");
        addItem(ModItems.FLASHLIGHT, "Фонарик");

        // Тултипы из AbstractLightItem
        add("tooltip.examplemod.status", "Состояние: ");
        add("tooltip.examplemod.status.on", "ВКЛ");
        add("tooltip.examplemod.status.off", "ВЫКЛ");

        // Тултипы конкретных предметов
        add("tooltip.examplemod.lighter.desc", "1");
        add("tooltip.examplemod.flashlight.desc", "2");
    }
}
