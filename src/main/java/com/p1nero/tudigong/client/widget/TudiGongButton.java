package com.p1nero.tudigong.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TudiGongButton extends Button {

    public TudiGongButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        boolean highlighted = this.active && this.isHoveredOrFocused();
        int alpha = Mth.ceil(this.alpha * 255.0F) << 24;
        int background = alpha | (this.active ? (highlighted ? 0x7B2525 : 0x451D20) : 0x282020);
        int border = alpha | (highlighted ? 0xE4C36A : 0x9B7040);
        int text = alpha | (this.active ? 0xF6E8C8 : 0x8A8074);

        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), background);
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + 1, border);
        guiGraphics.fill(this.getX(), this.getY() + this.getHeight() - 1, this.getX() + this.getWidth(), this.getY() + this.getHeight(), border);
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.getHeight(), border);
        guiGraphics.fill(this.getX() + this.getWidth() - 1, this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), border);
        this.renderString(guiGraphics, Minecraft.getInstance().font, text);
    }
}
