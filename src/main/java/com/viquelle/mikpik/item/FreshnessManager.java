package com.viquelle.mikpik.item;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.datagen.ModConfig;
import com.viquelle.mikpik.registry.ModDataComponents;
import com.viquelle.mikpik.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class FreshnessManager {

    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> CONTAINER_LAST_CHECK = new ConcurrentHashMap<>();
    private static int serverTickCounter = 0;
    private static final int PLAYER_TICK_INTERVAL = 20;
    private static final int BLOCK_TICK_INTERVAL = 40;
    private static final int ENTITY_TICK_INTERVAL = 60;

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof Level level && !level.isClientSide()) {
            BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
            if (be instanceof Container) {
                setLastCheckTime(level, event.getPos(), level.getGameTime());
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        Map<BlockPos, Long> levelMap = CONTAINER_LAST_CHECK.get(((Level)event.getLevel()).dimension());
        if (levelMap != null) {
            levelMap.remove(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide() || !(event.getChunk() instanceof LevelChunk chunk)) return;
        Level level = chunk.getLevel();
        long currentTick = level.getGameTime();

        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be instanceof Container) {
                setLastCheckTime(level, be.getBlockPos(), currentTick);
            }
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide() || !(event.getChunk() instanceof LevelChunk chunk)) return;
        Map<BlockPos, Long> levelMap = CONTAINER_LAST_CHECK.get(event.getChunk().getLevel().dimension());
        if (levelMap != null) {
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (be instanceof Container) {
                    levelMap.remove(be.getBlockPos());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        if (!ModConfig.ENABLE_SPOILING.get()) return;

        serverTickCounter++;
        if (serverTickCounter < BLOCK_TICK_INTERVAL) return;
        serverTickCounter = 0;

        for (Level level : event.getServer().getAllLevels()) {
            if (level.isClientSide()) continue;

            Map<BlockPos, Long> levelMap = CONTAINER_LAST_CHECK.get(level.dimension());
            if (levelMap == null || levelMap.isEmpty()) continue;

            long currentTick = level.getGameTime();
            var iterator = levelMap.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                BlockPos pos = entry.getKey();

                if (!level.hasChunkAt(pos)) {
                    iterator.remove();
                    continue;
                }

                BlockEntity be = level.getBlockEntity(pos);
                if (!(be instanceof Container container)) {
                    iterator.remove();
                    continue;
                }

                int deltaTicks = (int) (currentTick - entry.getValue());
                if (deltaTicks >= BLOCK_TICK_INTERVAL) {
                    float multiplier = calculateCoolingMultiplier(level, pos);

                    boolean hasEphemeralItem = false;
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        hasEphemeralItem |= applySpoilageToContainerSlot(
                                container, i, multiplier, deltaTicks
                        );
                    }

                    if (hasEphemeralItem) {
                        spawnAxeParticle(level, pos, BLOCK_TICK_INTERVAL / 5);
                    }

                    entry.setValue(currentTick);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !ModConfig.ENABLE_SPOILING.get()) return;

        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (be instanceof Container container) {
            long currentTick = event.getLevel().getGameTime();
            long lastCheck = getLastCheckTime(event.getLevel(), event.getPos());
            int deltaTicks = (int) (currentTick - lastCheck);

            if (deltaTicks > 0) {
                float multiplier = calculateCoolingMultiplier(event.getLevel(), event.getPos());
                boolean hasEphemeralItem = false;

                for (int i = 0; i < container.getContainerSize(); i++) {
                    hasEphemeralItem |= applySpoilageToContainerSlot(
                            container, i, multiplier, deltaTicks
                    );
                }

                if (hasEphemeralItem) {
                    spawnAxeParticle(event.getLevel(), event.getPos(), BLOCK_TICK_INTERVAL / 5);
                }

                setLastCheckTime(event.getLevel(), event.getPos(), currentTick);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre event) {
        if (!ModConfig.ENABLE_SPOILING.get() || event.getEntity().level().isClientSide()) return;
        Player player = event.getEntity();

        if (player.tickCount % PLAYER_TICK_INTERVAL != 0) return;

        if (isEphemeralItem(player.getMainHandItem()) ||
                isEphemeralItem(player.getOffhandItem())) {
            spawnAxeParticle(player.level(), player.getBoundingBox(), PLAYER_TICK_INTERVAL / 5);
        }

        float multiplier = ModConfig.MULT_INVENTORY.get().floatValue();
        if (player.isInWaterOrRain()) {
            multiplier *= ModConfig.MULT_RAIN_WATER.get().floatValue();
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            applySpoilageToContainerSlot(player.getInventory(), i, multiplier, 20);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!ModConfig.ENABLE_SPOILING.get() || event.getEntity().level().isClientSide()) return;

        Entity entity = event.getEntity();
        if (entity.tickCount % ENTITY_TICK_INTERVAL != 0) return;
        if (entity instanceof Player) return; // Игнорим, т.к мы уже тикаем отдельно игрока

        float multiplier;
        switch (entity) {
            case ItemEntity itemEntity -> {
                multiplier = getEntityEnvironmentMultiplier(entity, false);
                ItemStack stack = itemEntity.getItem();
                if (stack.isEmpty()) return;

                if (isEphemeralItem(stack)) {
                    spawnAxeParticle(itemEntity.level(), itemEntity.getBoundingBox(), ENTITY_TICK_INTERVAL / 5);
                }

                ItemStack before = stack.copy();

                if (applySpoilageToStack(stack, multiplier, ENTITY_TICK_INTERVAL)) {
                    itemEntity.setItem(getSpoiledResult(stack));
                } else if (!ItemStack.isSameItemSameComponents(before, stack)) {
                    itemEntity.setItem(stack);
                }
            }
            case Container container -> {
                multiplier = getEntityEnvironmentMultiplier(entity, true);
                boolean hasEphemeralItem = false;

                for (int i = 0; i < container.getContainerSize(); i++) {
                    hasEphemeralItem |= applySpoilageToContainerSlot(container, i, multiplier, ENTITY_TICK_INTERVAL);
                }

                if (hasEphemeralItem) {
                    spawnAxeParticle(entity.level(), entity.getBoundingBox(), ENTITY_TICK_INTERVAL / 5);
                }
            }
            case AbstractChestedHorse chestedHorse -> {
                multiplier = getEntityEnvironmentMultiplier(entity, true);
                Container container = chestedHorse.getInventory();
                if (container.isEmpty()) return;

                boolean hasEphemeralItem = false;

                for (int i = 0; i < container.getContainerSize(); i++) {
                    hasEphemeralItem |= applySpoilageToContainerSlot(container, i, multiplier, ENTITY_TICK_INTERVAL);
                }

                if (hasEphemeralItem) {
                    spawnAxeParticle(
                            chestedHorse.level(),
                            chestedHorse.getBoundingBox(),
                            ENTITY_TICK_INTERVAL / 5
                    );
                }
            }
            default -> {}
        }
    }

    private static float getEntityEnvironmentMultiplier(Entity entity, boolean isContainer) {
        float multiplier = 1f;
        boolean isColdBiome = entity.level().getBiome(entity.blockPosition()).is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS);
        if (isColdBiome) {
            multiplier *= ModConfig.MULT_COLD_BIOME.get().floatValue();
        }
        if (isContainer) {
            multiplier *= ModConfig.MULT_STORAGE.get().floatValue();
        } else {
            multiplier *= ModConfig.MULT_GROUND.get().floatValue();
            if (entity.isInWaterOrRain()) {
                multiplier *= ModConfig.MULT_RAIN_WATER.get().floatValue();
            }
        }

        return multiplier;
    }

    private static boolean applySpoilageToContainerSlot(
            Container container,
            int slot,
            float multiplier,
            int deltaTicks
    ) {
        ItemStack stack = container.getItem(slot);
        if (stack.isEmpty()) return false;

        boolean isEphemeral = isEphemeralItem(stack);
        ItemStack before = stack.copy();

        if (applySpoilageToStack(stack, multiplier, deltaTicks)) {
            container.setItem(slot, getSpoiledResult(stack));
        } else if (!ItemStack.isSameItemSameComponents(before, stack)) {
            container.setItem(slot, stack);
        }

        return isEphemeral;
    }

    /**
     * Эфемерные предметы получают визуальный эффект во время обычной проверки свежести.
     */
    private static boolean isEphemeralItem(ItemStack stack) {
        return stack.is(ModItems.EPHEMERAL_AXE.get());
    }

    private static void spawnAxeParticle(Level level, AABB box, int particleCount) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double centerX = (box.minX + box.maxX) * 0.5D;
        double centerY = (box.minY + box.maxY) * 0.5D;
        double centerZ = (box.minZ + box.maxZ) * 0.5D;

        double maxOffsetX = box.getXsize() * 0.5D + 0.18D;
        double maxOffsetY = box.getYsize() * 0.5D + 0.18D;
        double maxOffsetZ = box.getZsize() * 0.5D + 0.18D;

        for (int i = 0; i < particleCount; i++) {
            double x = centerX + (level.random.nextDouble() * 2.0D - 1.0D) * maxOffsetX;
            double y = centerY + (level.random.nextDouble() * 2.0D - 1.0D) * maxOffsetY;
            double z = centerZ + (level.random.nextDouble() * 2.0D - 1.0D) * maxOffsetZ;

            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    x, y, z,
                    1,
                    0.08D, 0.08D, 0.08D,
                    0.025D
            );
        }
    }

    private static void spawnAxeParticle(Level level, BlockPos pos, int particleCount) {
        if (!(level instanceof ServerLevel)) return;

        var shape = level.getBlockState(pos).getShape(level, pos);
        AABB box;

        if (shape.isEmpty()) {
            box = new AABB(pos);
        } else {
            AABB localBox = shape.bounds();
            box = new AABB(
                    pos.getX() + localBox.minX,
                    pos.getY() + localBox.minY,
                    pos.getZ() + localBox.minZ,
                    pos.getX() + localBox.maxX,
                    pos.getY() + localBox.maxY,
                    pos.getZ() + localBox.maxZ
            );
        }

        spawnAxeParticle(level, box, particleCount);
    }

    /// Возвращает БАЗОВОЕ или ИМЕЮЩЕЕСЯ БАЗОВОЕ время гниения, если предмет может гнить, иначе -1
    public static int shouldSpoiling(ItemStack stack) {
        Item item = stack.getItem();

        if (ModConfig.isInSpoilBlacklist(item)) return -1;
        if (stack.has(ModDataComponents.SPOIL_TIME)) return stack.get(ModDataComponents.SPOIL_TIME);
        int spoilTime = ModConfig.getCustomTime(item);
        if (spoilTime > 0) return spoilTime;

        if (stack.has(DataComponents.FOOD)) return ModConfig.DEFAULT_SPOIL_TIME.get();

        if (item.equals(ModItems.HAM_BAT.get())) {
            if (ModConfig.HAM_BAT_SPOILING.get()) return ModConfig.HAM_BAT_SPOIL_TIME.get();
            return -1;
        }

        return -1;
    }

    public static void applySpoilData(ItemStack stack, int spoilingTime, float remainingTime) {
        stack.set(ModDataComponents.SPOIL_TIME.get(), spoilingTime);
        stack.set(ModDataComponents.SPOIL_TIME_REMAINING.get(), remainingTime);
    }

    public static float getSpoilPercent(ItemStack stack) {
        if (stack.has(ModDataComponents.SPOIL_TIME)) {
            int time = stack.get(ModDataComponents.SPOIL_TIME);
            return stack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING, (float)time) / time;
        }
        return -1f;
    }

    public static void setSpoilPercent(ItemStack stack, float percent) {
        if (stack.has(ModDataComponents.SPOIL_TIME)) {
            stack.set(ModDataComponents.SPOIL_TIME_REMAINING, stack.get(ModDataComponents.SPOIL_TIME) * percent);
        }
    }

    private static boolean applySpoilageToStack(ItemStack stack, float multiplier, int deltaTicks) {
        if (stack.isEmpty()) return false;
        if (stack.is(ModItems.WRAPPER.get())) return false;

        if (stack.has(DataComponents.CONTAINER)) {
            ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
            if (contents != null && contents != ItemContainerContents.EMPTY) {
                int slots = contents.getSlots();
                NonNullList<ItemStack> items = NonNullList.withSize(slots, ItemStack.EMPTY);
                contents.copyInto(items);

                boolean changed = false;
                for (int i = 0; i < slots; i++) {
                    ItemStack child = items.get(i);
                    if (child.isEmpty()) continue;

                    ItemStack childBefore = child.copy();
                    boolean childSpoiled = applySpoilageToStack(child, multiplier, deltaTicks);

                    if (childSpoiled) {
                        items.set(i, getSpoiledResult(child));
                        changed = true;
                    } else if (!ItemStack.isSameItemSameComponents(childBefore, child)) {
                        items.set(i, child);
                        changed = true;
                    }
                }

                if (changed) {
                    stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
                }
            }
        }

        int targetSpoilTime = shouldSpoiling(stack);
        if (targetSpoilTime <= 0) return false;

        if (!stack.has(ModDataComponents.SPOIL_TIME.get())) {
            stack.set(ModDataComponents.SPOIL_TIME.get(), targetSpoilTime);
        }
        float timeRemaining = stack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING.get(), (float) targetSpoilTime);
        float deduction = deltaTicks * multiplier;
        float newTimeRemaining = timeRemaining - deduction;
        stack.set(ModDataComponents.SPOIL_LAST_REDUCTION.get(), multiplier);

        if (newTimeRemaining <= 0) {
            return true;
        } else {
            stack.set(ModDataComponents.SPOIL_TIME_REMAINING.get(), newTimeRemaining);
            return false;
        }
    }

    private static float calculateCoolingMultiplier(Level level, BlockPos pos) {
        float multiplier = ModConfig.MULT_STORAGE.get().floatValue();

        if (level.getBiome(pos).is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
            multiplier *= ModConfig.MULT_COLD_BIOME.get().floatValue();
        }

        for (Direction dir : Direction.values()) {
            var state = level.getBlockState(pos.relative(dir));
            if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) multiplier *= ModConfig.MULT_SNOW.get().floatValue();
            else if (state.is(Blocks.ICE)) multiplier *= ModConfig.MULT_ICE.get().floatValue();
            else if (state.is(Blocks.PACKED_ICE)) multiplier *= ModConfig.MULT_PACKED_ICE.get().floatValue();
            else if (state.is(Blocks.BLUE_ICE)) multiplier *= ModConfig.MULT_BLUE_ICE.get().floatValue();
        }

        return Math.max(ModConfig.MIN_TOTAL_STORAGE_MULT.get().floatValue(), multiplier);
    }

    private static ItemStack getSpoiledResult(ItemStack original) {
        MikpikMod.LOGGER.info("{}",original.getItem().toString());
        Item item = ModConfig.getCustomSpoilTransform(original.getItem());
        if (item != null) {
            return new ItemStack(item, original.getCount());
        }
        return new ItemStack(Blocks.DIRT, original.getCount());
    }

    private static long getLastCheckTime(Level level, BlockPos pos) {
        return CONTAINER_LAST_CHECK.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>())
                .getOrDefault(pos, level.getGameTime());
    }

    private static void setLastCheckTime(Level level, BlockPos pos, long tick) {
        CONTAINER_LAST_CHECK.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>())
                .put(pos, tick);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        int spoilTime = shouldSpoiling(stack);
        if (spoilTime <= 0) return;

        float remainingTime = stack.getOrDefault(ModDataComponents.SPOIL_TIME_REMAINING, (float)spoilTime);
        float avgRed = stack.getOrDefault(ModDataComponents.SPOIL_LAST_REDUCTION.get(), 0f);

        String formattedDays;
        if (avgRed < 0.001f) {
            formattedDays = "???";
        } else {
            float days = Math.max(0f, remainingTime / 24000.0f / avgRed);
            formattedDays = String.format(Locale.ROOT, "%.1f", days); // 1 знак после запятой
        }

        Component spoilTooltip = Component.translatable("tooltip." + MikpikMod.MODID + ".spoils_in", formattedDays)
                .withStyle(ChatFormatting.GRAY);

        event.getToolTip().add(spoilTooltip);
    }
}
