package com.viquelle.mikpik.client;

import com.viquelle.mikpik.ICampfireFuel;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.network.payload.CampfireCookRequestPayload;
import com.viquelle.mikpik.util.CampfireCookingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

@EventBusSubscriber(modid = MikpikMod.MODID, value = Dist.CLIENT)
public class ClientCampfireHandler {
    private static int holdTicks = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || !(mc.hitResult instanceof BlockHitResult blockHit)) {
            if (holdTicks > 0) {
                holdTicks = Math.max(holdTicks - 2, 0);
            }
            return;
        }

        var state = player.level().getBlockState(blockHit.getBlockPos());

        if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
            ItemStack handItem = player.getMainHandItem();
            boolean isCookable = CampfireCookingHelper.getCookingRecipe(player.level(), handItem) != null;

            if (isCookable && mc.options.keyUse.isDown()) {
                holdTicks++;

                if (holdTicks >= CampfireCookingHelper.MAX_COOK_TIME) {
                    PacketDistributor.sendToServer(new CampfireCookRequestPayload(blockHit.getBlockPos()));
                    holdTicks = 0;
                }
            } else {
                if (holdTicks > 0) {
                    holdTicks = Math.max(holdTicks - 2, 0);
                }
            }
        } else {
            holdTicks = Math.max(holdTicks - 2, 0);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.hitResult == null || mc.player == null) return;

        if (mc.hitResult instanceof BlockHitResult blockHit) {
            var state = mc.player.level().getBlockState(blockHit.getBlockPos());

            if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
                ItemStack handItem = mc.player.getMainHandItem();

                int currentFuel = 0;
                if (mc.player.level().getBlockEntity(blockHit.getBlockPos()) instanceof ICampfireFuel fuelBe) {
                    currentFuel = fuelBe.mikpik$getFuelTime();
                }

                boolean isCookable = CampfireCookingHelper.getCookingRecipe(mc.player.level(), handItem) != null;
                boolean isFuel = CampfireCookingHelper.getFuelValue(handItem) > 0;
                boolean isHoldingUse = mc.options.keyUse.isDown();

                int width = mc.getWindow().getGuiScaledWidth();
                int height = mc.getWindow().getGuiScaledHeight();
                int centerX = width / 2;
                int baseY = height / 2 + 40;

                if (isCookable && isHoldingUse) {
                    float progress = Math.min(holdTicks / (float) CampfireCookingHelper.MAX_COOK_TIME, 1.0f);

                    int barWidth = 180;
                    int barHeight = 10;
                    int x = centerX - barWidth / 2;
                    int y = baseY;

                    event.getGuiGraphics().fill(x, y, x + barWidth, y + barHeight, 0x99000000);

                    int filledWidth = (int) (barWidth * progress);
                    event.getGuiGraphics().fill(x, y, x + filledWidth, y + barHeight, 0xFFFF8C00);

                    return;
                }

                if (isCookable) {
                    Component text = Component.translatable("gui." + MikpikMod.MODID + ".campfire_hold_to_cook");
                    event.getGuiGraphics().drawCenteredString(mc.font, text, centerX, baseY, 0xFFFFFF);
                    return;
                }

                if (isFuel) {
                    int fuelValue = CampfireCookingHelper.getFuelValue(handItem);
                    Component currentTimeStr = formatTime(currentFuel);
                    Component bonusTimeStr = formatTime(fuelValue);

                    Component line1 = Component.translatable("gui." + MikpikMod.MODID + ".campfire_current_fuel", currentTimeStr.getString());
                    Component line2 = Component.translatable("gui." + MikpikMod.MODID + ".campfire_fuel_bonus", bonusTimeStr.getString())
                            .withStyle(style -> style.withColor(0xFFFFAA00));

                    event.getGuiGraphics().drawCenteredString(mc.font, line1, centerX, baseY - 10, 0xFFFFFF);
                    event.getGuiGraphics().drawCenteredString(mc.font, line2, centerX, baseY + 5, 0xFFFFFF);
                    return;
                }

                Component currentTimeStr = formatTime(currentFuel);
                Component text = Component.translatable("gui." + MikpikMod.MODID + ".campfire_fuel_status", currentTimeStr.getString());
                event.getGuiGraphics().drawCenteredString(mc.font, text, centerX, baseY, 0xFFFFFF);
            }
        }
    }

    private static Component formatTime(int ticks) {
        if (ticks <= 0) {
            return Component.translatable("gui." + MikpikMod.MODID + ".campfire_time_zero");
        }
        float days = ticks / 24000f;
        if (days >= 1.0f) {
            String sDays = String.format(Locale.ROOT, "%.2f", days);
            return Component.translatable("gui." + MikpikMod.MODID + ".campfire_time_days", sDays);
        } else {
            float hours = ticks / 1000f;
            String sHours = String.format(Locale.ROOT, "%.1f", hours);
            String sDays = String.format(Locale.ROOT, "%.2f", days);
            return Component.translatable("gui." + MikpikMod.MODID + ".campfire_time_hours_days", sHours, sDays);
        }
    }
}