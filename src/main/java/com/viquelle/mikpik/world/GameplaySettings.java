package com.viquelle.mikpik.world;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.network.payload.GameplayModePayload;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class GameplaySettings extends SavedData {
    private static final String NAME = MikpikMod.MODID + "_settings";
    private GameplayMode mode = GameplayMode.DST;

    public GameplayMode getMode() {
        return mode;
    }

    public void setMode(GameplayMode mode) {
        this.mode = mode;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        compoundTag.putString("Mode", mode.name());
        return compoundTag;
    }

    public static GameplaySettings load(CompoundTag tag, HolderLookup.Provider registries) {
        GameplaySettings settings = new GameplaySettings();

        String name = tag.getString("Mode");

        try {
            settings.mode = GameplayMode.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            settings.mode = GameplayMode.DST;
        }

        return settings;
    }

    public static GameplaySettings get(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();

        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        GameplaySettings::new,
                        GameplaySettings::load
                ),
                NAME
        );
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        PacketDistributor.sendToPlayer(player, new GameplayModePayload(Gameplay.get(player.serverLevel())));
    }
}

