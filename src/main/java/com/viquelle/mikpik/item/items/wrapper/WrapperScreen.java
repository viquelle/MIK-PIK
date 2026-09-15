package com.viquelle.mikpik.item.items.wrapper;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class WrapperScreen extends AbstractContainerScreen<WrapperMenu> {
    private static final ResourceLocation BOX_TEXTURE = ResourceLocation.fromNamespaceAndPath("mikpik", "textures/item/wrapper_empty.png");
    private static final ResourceLocation BOX1_TEXTURE = ResourceLocation.fromNamespaceAndPath("mikpik", "textures/item/wrapper_full.png");
    private static final ResourceLocation INVENTORY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

    private Button wrapButton;

    public WrapperScreen(WrapperMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = WrapperLayout.GUI_WIDTH;
        this.imageHeight = WrapperLayout.GUI_HEIGHT;
        this.inventoryLabelY = this.imageHeight - 92;
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        int buttonW = 70;
        int buttonH = 20;
        int buttonX = this.leftPos + WrapperLayout.CENTER_START_X + (int) (WrapperLayout.SLOT_SIZE * 1.5f);
        int buttonY = this.topPos + WrapperLayout.CENTER_START_Y + (int)(WrapperLayout.SLOT_SIZE * (3f + 3.5f));

        this.wrapButton = Button.builder(Component.translatable("gui." + MikpikMod.MODID + ".wrap"), btn -> {
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
                })
                .bounds(buttonX - buttonW / 2, buttonY, buttonW, buttonH)
                .build();

        this.addRenderableWidget(this.wrapButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        boolean hasItems = false;
        for (int i = 0; i < 9; i++) {
            if (!this.menu.getWrapperContainer().getItem(i).isEmpty()){
                hasItems = true;
                break;
            }
        }
        this.wrapButton.active = hasItems;

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        if (this.wrapButton != null) {
            guiGraphics.blit(BOX1_TEXTURE,
                    this.wrapButton.getX() + 2,
                    this.wrapButton.getY() + 2,
                    0, 0,
                    16,16,16,16);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos + WrapperLayout.TOP_MARGIN;

        int wrapperWidth = 240;
        int wrapperHeight = 240;
        int wrapperX = x + WrapperLayout.CENTER_START_X + (int) (WrapperLayout.SLOT_SIZE * 1.5) - wrapperWidth / 2 ;
        int wrapperY = y + WrapperLayout.CENTER_START_Y + (int) (WrapperLayout.SLOT_SIZE * 1.5) - wrapperHeight / 2;

        guiGraphics.setColor(0.6f, 0.6f, 0.6f, 1.0f);
        guiGraphics.blit(BOX_TEXTURE,
                wrapperX, wrapperY,
                0, 0,
                wrapperWidth, wrapperHeight,
                wrapperWidth, wrapperHeight);

        int slotX = this.leftPos + WrapperLayout.CENTER_START_X;
        int slotY = this.topPos + WrapperLayout.TOP_MARGIN + WrapperLayout.CENTER_START_Y;

        guiGraphics.setColor(0.4f, 0.4f, 0.4f, 0.5f);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                guiGraphics.fill(
                        slotX + j * WrapperLayout.SLOT_SIZE,
                        slotY + i * WrapperLayout.SLOT_SIZE,
                        slotX + (j + 1) * WrapperLayout.SLOT_SIZE - 2 ,
                        slotY + (i + 1) * WrapperLayout.SLOT_SIZE - 2,
                        0xFF2B2B2B
                        );

            }
        }
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);


       guiGraphics.blit(INVENTORY_TEXTURE, x, y + WrapperLayout.INV_START_Y, 0, 126, 176, 96);
    }

}