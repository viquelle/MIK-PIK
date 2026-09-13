package com.viquelle.mikpik.datagen;

import com.viquelle.mikpik.MikpikMod;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class ModConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue AMBIENT_BRIGHTNESS;
    public static final ModConfigSpec.BooleanValue ENABLE_SPOILING;
    public static final ModConfigSpec.IntValue DEFAULT_SPOIL_TIME;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_TIMES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_SPOIL_TRANSFORM;

    public static final ModConfigSpec.IntValue HAM_BAT_SPOIL_TIME;
    public static final ModConfigSpec.BooleanValue HAM_BAT_SPOILING;

    public static final ModConfigSpec.DoubleValue MULT_INVENTORY;
    public static final ModConfigSpec.DoubleValue MULT_GROUND;
    public static final ModConfigSpec.DoubleValue MULT_STORAGE;
    public static final ModConfigSpec.DoubleValue MIN_TOTAL_STORAGE_MULT;
    public static final ModConfigSpec.DoubleValue MULT_RAIN_WATER;
    public static final ModConfigSpec.DoubleValue MULT_SNOW;
    public static final ModConfigSpec.DoubleValue MULT_ICE;
    public static final ModConfigSpec.DoubleValue MULT_PACKED_ICE;
    public static final ModConfigSpec.DoubleValue MULT_BLUE_ICE;
    public static final ModConfigSpec.DoubleValue MULT_COLD_BIOME;

    public static final ModConfigSpec.IntValue MAX_CAMP_FUEL_TIME;
    public static final ModConfigSpec.IntValue INITIAL_CAMP_FUEL_TIME;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> FUEL_VALUES;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> FUEL_BLACKLIST;

    private static final Object2IntOpenHashMap<Item> CUSTOM_SPOIL_TIMES_CACHE = new Object2IntOpenHashMap<>();
    private static final ObjectOpenHashSet<Item> CUSTOM_SPOIL_BLACKLIST_CACHE = new ObjectOpenHashSet<>();
    private static final Object2ObjectOpenHashMap<Item, Item> SPOIL_TRANSFORM_CACHE = new Object2ObjectOpenHashMap<>();
    private static final ObjectOpenHashSet<Item> FUEL_BLACKLIST_CACHE = new ObjectOpenHashSet<>();
    private static final Object2IntOpenHashMap<Item> DIRECT_FUEL_VALUES_CACHE = new Object2IntOpenHashMap<>();
    private static final List<TagFuelRule> TAG_FUEL_RULES_CACHE = new ArrayList<>();
    private record TagFuelRule(TagKey<Item> tag, int value) {}

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");

        AMBIENT_BRIGHTNESS = builder
                .comment("How bright is the eyes adaptation light (0.0 to 1.0)")
                .defineInRange("ambient_brightness", 0.3, 0.0, 1.0);

        ENABLE_SPOILING = builder
                .comment("Enable or disable the food spoiling mechanic.")
                .define("enable_spoiling", true);

        DEFAULT_SPOIL_TIME = builder
                .comment("Base spoil time for food items not explicitly defined in the config.")
                .defineInRange("default_spoil_time", 168000, 1, 10000000);

        BLACKLIST = builder
                .comment("List of food items that should NOT spoil (has priority).")
                .defineList("blacklist", List.of(
                        "minecraft:golden_apple",
                        "minecraft:enchanted_golden_apple",
                        "minecraft:rotten_flesh",
                        "minecraft:dried_kelp",
                        "minecraft:chorus_fruit",
                        "minecraft:honey_bottle",
                        "minecraft:ominous_bottle"
                ), () -> "", item -> item instanceof String); // () -> "" enables "Add" button in config GUI

        CUSTOM_TIMES = builder
                .comment("Custom spoil times for specific food items.",
                        "Syntax: item_id=spoil_ticks",
                        "Example: \"minecraft:salmon=48000\"")
                .defineList("custom_times", List.of(
                        "minecraft:apple=216000",
                        "minecraft:baked_potato=168000",
                        "minecraft:beef=96000",
                        "minecraft:beetroot=336000",
                        "minecraft:beetroot_soup=96000",
                        "minecraft:bread=216000",
                        "minecraft:brown_mushroom=120000",
                        "minecraft:carrot=336000",
                        "minecraft:chicken=72000",
                        "minecraft:cocoa_beans=168000",
                        "minecraft:cod=48000",
                        "minecraft:cooked_beef=168000",
                        "minecraft:cooked_chicken=144000",
                        "minecraft:cooked_cod=144000",
                        "minecraft:cooked_mutton=168000",
                        "minecraft:cooked_porkchop=168000",
                        "minecraft:cooked_rabbit=168000",
                        "minecraft:cooked_salmon=144000",
                        "minecraft:cookie=168000",
                        "minecraft:egg=216000",
                        "minecraft:glow_berries=168000",
                        "minecraft:melon=240000",
                        "minecraft:melon_slice=120000",
                        "minecraft:milk_bucket=72000",
                        "minecraft:mushroom_stew=96000",
                        "minecraft:mutton=96000",
                        "minecraft:porkchop=96000",
                        "minecraft:potato=336000",
                        "minecraft:pufferfish=48000",
                        "minecraft:pumpkin=336000",
                        "minecraft:pumpkin_pie=168000",
                        "minecraft:rabbit=96000",
                        "minecraft:rabbit_stew=96000",
                        "minecraft:red_mushroom=120000",
                        "minecraft:salmon=48000",
                        "minecraft:sugar=216000",
                        "minecraft:suspicious_stew=48000",
                        "minecraft:sweet_berries=168000",
                        "minecraft:tropical_fish=48000",
                        "minecraft:wheat=216000"
                ), () -> "", item -> item instanceof String); // () -> "" enables "Add" button in config GUI

        CUSTOM_SPOIL_TRANSFORM = builder
                .comment(
                        "List of custom item transformations when spoiling.",
                        "Syntax: source_item_id=transformed_item_id",
                        "Example: \"minecraft:milk_bucket=minecraft:bucket\""
                )
                .defineList("custom_spoil_transform", List.of(
                        "minecraft:milk_bucket=minecraft:bucket",
                        "minecraft:potato=minecraft:poisonous_potato"
                ), () -> "", item -> item instanceof String);

        builder.pop();

        builder.push("ham_bat");
        HAM_BAT_SPOIL_TIME = builder
                .comment("Spoil time for the ham bat item.")
                .defineInRange("spoil_time", 144000, 1, 10000000);

        HAM_BAT_SPOILING = builder
                .comment("If true, the Ham Bat will spoil over time.")
                .define("is_spoiling", true);
        builder.pop();

        builder.push("environment");
        MULT_INVENTORY = builder
                .comment("Spoil time multiplier for items in player inventory.")
                .defineInRange("inventory_multiplier", 1.0, 0.0, 10.0);

        MULT_GROUND = builder
                .comment("Spoil time multiplier for items dropped on the ground.")
                .defineInRange("ground_multiplier", 1.5, 0.0, 10.0);

        MULT_STORAGE = builder
                .comment("Spoil time multiplier for items in storage blocks (chests, etc.).")
                .defineInRange("storage_multiplier", 0.5, 0.0, 10.0);

        MIN_TOTAL_STORAGE_MULT = builder
                .comment("Minimum total multiplier applied to items in storage.")
                .defineInRange("min_total_storage_mult", 0.1, 0.0, 10.0);

        MULT_RAIN_WATER = builder
                .comment("Spoil time multiplier when in rain or water.")
                .defineInRange("rain_water_multiplier", 2.0, 0.0, 10.0);

        MULT_SNOW = builder
                .comment("Spoil time multiplier when in snow.")
                .defineInRange("snow_multiplier", 0.95, 0.0, 10.0);

        MULT_ICE = builder
                .comment("Spoil time multiplier when on ice.")
                .defineInRange("ice_multiplier", 0.85, 0.0, 10.0);

        MULT_PACKED_ICE = builder
                .comment("Spoil time multiplier when on packed ice.")
                .defineInRange("packed_ice_multiplier", 0.75, 0.0, 10.0);

        MULT_BLUE_ICE = builder
                .comment("Spoil time multiplier when on blue ice.")
                .defineInRange("blue_ice_multiplier", 0.65, 0.0, 10.0);

        MULT_COLD_BIOME = builder
                .comment("Spoil time multiplier when in a cold biome.")
                .defineInRange("cold_biome_multiplier", 0.85, 0.0, 10.0);

        builder.pop();

        builder.push("campfire fuel");

        MAX_CAMP_FUEL_TIME = builder
                .comment("This value should be bigger than INITIAL_CAMP_FUEL_TIME")
                .defineInRange("max_camp_fuel_time", 18000, 1, Integer.MAX_VALUE);

        INITIAL_CAMP_FUEL_TIME = builder
                .comment("Initial campfire fuel duration in ticks.")
                .defineInRange("initial_camp_fuel_time", 18000, 0, 999999999);

        FUEL_VALUES = builder
                .comment(
                        "Defines fuel values for items and tags.",
                        "Syntax: item_id=ticks OR #tag_id=ticks",
                        "Examples: 'minecraft:coal=1600', '#minecraft:logs=300'",
                        "If an item matches multiple tags, the first match in this list is used."
                )
                .defineList("fuel_values", List.of(
                        "#minecraft:bamboo_blocks=1600",
                        "#minecraft:banners=800",
                        "#minecraft:boats=2000",
                        "#minecraft:fence_gates=1600",
                        "#minecraft:hanging_signs=800",
                        "#minecraft:logs=1600",
                        "#minecraft:planks=400",
                        "#minecraft:saplings=200",
                        "#minecraft:signs=600",
                        "#minecraft:wooden_axes=1600",
                        "#minecraft:wooden_buttons=100",
                        "#minecraft:wooden_doors=800",
                        "#minecraft:wooden_fences=1600",
                        "#minecraft:wooden_hoes=1000",
                        "#minecraft:wooden_pickaxes=1600",
                        "#minecraft:wooden_pressure_plates=400",
                        "#minecraft:wooden_shovels=600",
                        "#minecraft:wooden_slabs=200",
                        "#minecraft:wooden_stairs=300",
                        "#minecraft:wooden_swords=1000",
                        "#minecraft:wooden_trapdoors=400",
                        "#minecraft:wool=400",
                        "#minecraft:wool_carpets=200",
                        "minecraft:azalea=200",
                        "minecraft:bamboo=100",
                        "minecraft:bamboo_mosaic=400",
                        "minecraft:bamboo_mosaic_slab=200",
                        "minecraft:bamboo_mosaic_stairs=300",
                        "minecraft:barrel=2400",
                        "minecraft:blaze_rod=2400",
                        "minecraft:bookshelf=2800",
                        "minecraft:bow=1000",
                        "minecraft:bowl=200",
                        "minecraft:cartography_table=1600",
                        "minecraft:charcoal=1600",
                        "minecraft:chest=3200",
                        "minecraft:chiseled_bookshelf=2800",
                        "minecraft:coal=1600",
                        "minecraft:coal_block=16000",
                        "minecraft:composter=2800",
                        "minecraft:crafting_table=1600",
                        "minecraft:crossbow=1600",
                        "minecraft:daylight_detector=800",
                        "minecraft:dead_bush=100",
                        "minecraft:dried_kelp_block=4000",
                        "minecraft:fishing_rod=800",
                        "minecraft:fletching_table=1600",
                        "minecraft:flowering_azalea=200",
                        "minecraft:jukebox=3200",
                        "minecraft:ladder=1000",
                        "minecraft:lectern=2400",
                        "minecraft:loom=1600",
                        "minecraft:mangrove_roots=1600",
                        "minecraft:note_block=3200",
                        "minecraft:scaffolding=400",
                        "minecraft:smithing_table=2400",
                        "minecraft:stick=200",
                        "minecraft:trapped_chest=3200"
                ), () -> "", val -> val instanceof String);

        FUEL_BLACKLIST = builder
                .comment(
                        "Items that will NEVER act as fuel, even if they are in a burnable tag.",
                        "Syntax: item_id",
                        "Example: 'minecraft:stick' (if you want to save sticks from burning)"
                )
                .defineList("blacklist", List.of(
                ), () -> "", val -> val instanceof String);

        builder.pop();

        SPEC = builder.build();
    }

    public static void rebuildCache() {
        CUSTOM_SPOIL_TIMES_CACHE.clear();
        CUSTOM_SPOIL_BLACKLIST_CACHE.clear();
        SPOIL_TRANSFORM_CACHE.clear();
        FUEL_BLACKLIST_CACHE.clear();
        DIRECT_FUEL_VALUES_CACHE.clear();
        TAG_FUEL_RULES_CACHE.clear();

        for (String entry : CUSTOM_TIMES.get()) {
            parseItemIntEntry(entry, CUSTOM_SPOIL_TIMES_CACHE);
        }

        for (String entry : BLACKLIST.get()) {
            CUSTOM_SPOIL_BLACKLIST_CACHE.add(getItemFromString(entry));
        }

        for (String entry : CUSTOM_SPOIL_TRANSFORM.get()) {
            if (entry == null) continue;
            String[] parts = entry.split("=", 2);
            if (parts.length == 2) {
                Item source = getItemFromString(parts[0].trim());
                Item target = getItemFromString(parts[1].trim());
                if (source != Items.AIR && target != Items.AIR) {
                    SPOIL_TRANSFORM_CACHE.put(source, target);
                }
            }
        }

        if (FUEL_BLACKLIST.get() != null) {
            for (String entry : FUEL_BLACKLIST.get()) {
                Item item = getItemFromString(entry.trim());
                if (item != Items.AIR) {
                    FUEL_BLACKLIST_CACHE.add(item);
                }
            }
        }

        if (FUEL_VALUES.get() != null) {
            for (String entry : FUEL_VALUES.get()) {
                if (entry == null) continue;
                String[] parts = entry.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    int value;
                    try {
                        value = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    if (key.startsWith("#")) {
                        // Это тег
                        try {
                            ResourceLocation tagRl = ResourceLocation.parse(key.substring(1));
                            TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagRl);
                            TAG_FUEL_RULES_CACHE.add(new TagFuelRule(tagKey, value));
                        } catch (Exception ignored) {}
                    } else {
                        // Это предмет
                        Item item = getItemFromString(key);
                        if (item != Items.AIR) {
                            DIRECT_FUEL_VALUES_CACHE.put(item, value);
                        }
                    }
                }
            }
        }

    }

    private static void parseItemIntEntry(String entry, Object2IntOpenHashMap<Item> map) {
        if (entry == null) return;
        String[] parts = entry.split("=", 2);
        if (parts.length == 2) {
            Item item = getItemFromString(parts[0].trim());
            if (item != Items.AIR) {
                try {
                    int ticks = Integer.parseInt(parts[1].trim());
                    map.put(item, ticks);
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    private static Item getItemFromString(String id) {
        try {
            ResourceLocation rl = ResourceLocation.parse(id);
            Item item = BuiltInRegistries.ITEM.get(rl);
            return item != null ? item : Items.AIR;
        } catch (Exception e) {
            return Items.AIR;
        }
    }

    public static boolean isInSpoilBlacklist(Item item) {
        return CUSTOM_SPOIL_BLACKLIST_CACHE.contains(item);
    }

    public static int getCustomTime(Item item) {
        return CUSTOM_SPOIL_TIMES_CACHE.getOrDefault(item, -1);
    }

    @Nullable
    public static Item getCustomSpoilTransform(Item item) {
        return SPOIL_TRANSFORM_CACHE.get(item);
    }

    public static int getFuelValue(Item item) {
        if (item == Items.AIR) return -1;

        if (FUEL_BLACKLIST_CACHE.contains(item)) return -1;

        if (DIRECT_FUEL_VALUES_CACHE.containsKey(item)) {
            return DIRECT_FUEL_VALUES_CACHE.getInt(item);
        }
        for (TagFuelRule rule : TAG_FUEL_RULES_CACHE) {
            if (item.builtInRegistryHolder().is(rule.tag)) {
                return rule.value;
            }
        }

        return -1;
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            rebuildCache();
        }
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            rebuildCache();
        }
    }
}