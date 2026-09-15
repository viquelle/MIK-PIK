package com.viquelle.mikpik.datagen;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.registry.ModBlocks;
import com.viquelle.mikpik.registry.ModItems;
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
        addItem(ModItems.FLOWER_CROWN, "Flower crown");
        addItem(ModItems.PIBBLE, "Pibble");
        addItem(ModItems.PLUSHY, "Plushy");
        addItem(ModItems.HEART, "Heart");
        addItem(ModItems.MAGNETLAMPE, "Magnetlampe");
        addItem(ModItems.LIFE_INJECTOR, "Cleansing Syringe");
        addItem(ModItems.HAM_BAT, "Ham bat");
        addItem(ModItems.WRAPPER, "Wrapper");

        addBlock(ModBlocks.MEAT_EFFIGY, "Meat effigy");

        // Клавиши
        add("key.categories." + MikpikMod.MODID, "Mikpik Mod");
        add("key." + MikpikMod.MODID + ".ghost_resurrect", "Resurrect");

        // Сообщения
        add("message." + MikpikMod.MODID + ".effigy_bound", "Effigy powered and bound to you!");
        add("message." + MikpikMod.MODID + ".effigy_needs_charged_heart", "You need a CHARGED heart to activate the effigy.");
        add("message." + MikpikMod.MODID + ".effigy_already_powered", "This effigy is already powered.");
        add("message." + MikpikMod.MODID + ".effigy_is_yours", "This effigy is bound to you.");
        add("message." + MikpikMod.MODID + ".effigy_is_someone_elses", "This is someone else's effigy.");
        add("message." + MikpikMod.MODID + ".effigy_revived", "You revived at the effigy!");

        add("message." + MikpikMod.MODID + ".wind_perfect", "✓ Perfect wind!");
        add("message." + MikpikMod.MODID + ".wind_good", "Good wind");
        add("message." + MikpikMod.MODID + ".wind_weak", "Weak wind");
        add("message." + MikpikMod.MODID + ".wind_miss", "✗ Miss!");

        // GUI
        add("gui." + MikpikMod.MODID + ".ghost_resurrect", "Hold [%s] to resurrect");
        add("gui." + MikpikMod.MODID + ".ghost_resurrect_effigy", "Hold [%s] to resurrect at Effigy");

        // Campfire
        add("gui." + MikpikMod.MODID + ".campfire_fuel_status", "Fuel: %s");
        add("gui." + MikpikMod.MODID + ".campfire_current_fuel", "Current fuel: %s");
        add("gui." + MikpikMod.MODID + ".campfire_fuel_bonus", "Will add: +%s");
        add("gui." + MikpikMod.MODID + ".campfire_hold_to_cook", "Hold RMB to cook");

        add("gui." + MikpikMod.MODID + ".campfire_time_zero", "0 hrs");
        add("gui." + MikpikMod.MODID + ".campfire_time_days", "%s days");
        add("gui." + MikpikMod.MODID + ".campfire_time_hours_days", "%s hrs (%s days)");
        add("gui." + MikpikMod.MODID + ".wrapper", "Wrapper");
        add("gui." + MikpikMod.MODID + ".wrap", "Wrap");

        // Подсказки (Tooltips)
        add("tooltip." + MikpikMod.MODID + ".heart_cant_kill", "This item can't kill you");
        add("tooltip." + MikpikMod.MODID + ".heart_charge", "Charge: %s/%s");
        add("tooltip." + MikpikMod.MODID + ".heart_ready", "Ready to revive");
        add("tooltip." + MikpikMod.MODID + ".spoils_in", "Will spoil in %s days");
    }

    private void add_ru() {
        add("itemGroup." + MikpikMod.MODID, "МЫК ПЫК");

        // Предметы
        addItem(ModItems.FLOWER_CROWN, "Цветочный венок");
        addItem(ModItems.PIBBLE, "Пиббл");
        addItem(ModItems.PLUSHY, "Плюшик");
        addItem(ModItems.HEART, "Сердце");
        addItem(ModItems.MAGNETLAMPE, "Заводной фонарь");
        addItem(ModItems.MEAT_EFFIGY, "Мясное чучело");
        addItem(ModItems.LIFE_INJECTOR, "Очищающий шприц");
        addItem(ModItems.HAM_BAT, "Мясная бита");
        addItem(ModItems.WRAPPER, "Обертка");

        add("entity." + MikpikMod.MODID + ".meat_effigy", "Мясное чучело");

        // Клавиши
        add("key.categories." + MikpikMod.MODID, "Mikpik Mod");
        add("key." + MikpikMod.MODID + ".ghost_resurrect", "Воскреситься");

        // Сообщения
        add("message." + MikpikMod.MODID + ".effigy_bound", "Чучело запитано и привязано к тебе!");
        add("message." + MikpikMod.MODID + ".effigy_needs_charged_heart", "Нужно ЗАРЯЖЕННОЕ сердце, чтобы активировать чучело.");
        add("message." + MikpikMod.MODID + ".effigy_already_powered", "Это чучело уже запитано.");
        add("message." + MikpikMod.MODID + ".effigy_is_yours", "Это чучело привязано к вам.");
        add("message." + MikpikMod.MODID + ".effigy_is_someone_elses", "Это чье-то чучело.");
        add("message." + MikpikMod.MODID + ".effigy_revived", "Ты возродился на чучеле!");

        add("message." + MikpikMod.MODID + ".wind_perfect", "✓ Идеальная заводка!");
        add("message." + MikpikMod.MODID + ".wind_good", "Хорошая заводка");
        add("message." + MikpikMod.MODID + ".wind_weak", "Слабая заводка");
        add("message." + MikpikMod.MODID + ".wind_miss", "✗ Мимо!");

        // GUI
        add("gui." + MikpikMod.MODID + ".ghost_resurrect", "Удерживайте [%s] для воскрешения");
        add("gui." + MikpikMod.MODID + ".ghost_resurrect_effigy", "Удерживайте [%s] для воскрешения на чучеле");

        // Костер
        add("gui." + MikpikMod.MODID + ".campfire_fuel_status", "Топливо: %s");
        add("gui." + MikpikMod.MODID + ".campfire_current_fuel", "Текущее топливо: %s");
        add("gui." + MikpikMod.MODID + ".campfire_fuel_bonus", "Добавит: +%s");
        add("gui." + MikpikMod.MODID + ".campfire_hold_to_cook", "Зажмите ПКМ, чтобы готовить");

        add("gui." + MikpikMod.MODID + ".campfire_time_zero", "0 ч.");
        add("gui." + MikpikMod.MODID + ".campfire_time_days", "%s дн.");
        add("gui." + MikpikMod.MODID + ".campfire_time_hours_days", "%s ч. (%s дн.)");
        add("gui." + MikpikMod.MODID + ".wrapper", "Обёртка");
        add("gui." + MikpikMod.MODID + ".wrap", "Упаковать");

        // Подсказки (Tooltips)
        add("tooltip." + MikpikMod.MODID + ".heart_cant_kill", "Этот предмет не может вас убить");
        add("tooltip." + MikpikMod.MODID + ".heart_charge", "Заряд: %s/%s");
        add("tooltip." + MikpikMod.MODID + ".heart_ready", "Готово к воскрешению");
        add("tooltip." + MikpikMod.MODID + ".spoils_in", "Испортится через %s дней");
    }
}