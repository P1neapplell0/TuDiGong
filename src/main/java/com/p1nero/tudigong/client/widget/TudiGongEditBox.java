package com.p1nero.tudigong.client.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class TudiGongEditBox extends EditBox {
    private final int outerX;
    private final int outerY;
    private final int outerWidth;
    private final int outerHeight;

    public TudiGongEditBox(Font font, int x, int y, int width, int height, Component hint) {
        super(font, x + 7, y + 6, width - 14, height - 8, hint);
        this.outerX = x;
        this.outerY = y;
        this.outerWidth = width;
        this.outerHeight = height;
        this.setBordered(false);
        this.setHint(hint);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.outerX && mouseX < this.outerX + this.outerWidth
                && mouseY >= this.outerY && mouseY < this.outerY + this.outerHeight;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = this.getX() - 7;
        int top = this.getY() - 6;
        int right = this.getX() + this.getWidth() + 7;
        int bottom = this.getY() + this.getHeight() + 2;
        int border = this.isFocused() ? 0xFFE4C36A : 0xFF8E6B43;
        guiGraphics.fill(left, top, right, bottom, 0xD8181513);
        guiGraphics.fill(left, top, right, top + 1, border);
        guiGraphics.fill(left, bottom - 1, right, bottom, border);
        guiGraphics.fill(left, top, left + 1, bottom, border);
        guiGraphics.fill(right - 1, top, right, bottom, border);
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
    }
}
