package com.p1nero.tudigong.client.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class TudiGongUiTheme {
    public static final int PANEL = 0xE61B1714;
    public static final int PANEL_INNER = 0xD628211C;
    public static final int GOLD = 0xFFE0BE69;
    public static final int GOLD_MUTED = 0xFF8E6B43;
    public static final int INK = 0xFFF5E8CA;
    public static final int CINNABAR = 0xFF7B2525;

    private TudiGongUiTheme() {
    }

    public static void renderBackdrop(GuiGraphics graphics, int width, int height, int left, int top, int right, int bottom, float progress) {
        int dimAlpha = (int) (150 * progress);
        graphics.fill(0, 0, width, height, dimAlpha << 24 | 0x090807);
        graphics.fill(left, top, right, bottom, withAlpha(PANEL, progress));
        graphics.fill(left + 3, top + 3, right - 3, bottom - 3, withAlpha(PANEL_INNER, progress));
        drawBorder(graphics, left, top, right, bottom, withAlpha(GOLD_MUTED, progress));

        int half = (int) ((right - left - 32) * 0.5F * easeOut(progress));
        int center = (left + right) / 2;
        graphics.fill(center - half, top + 8, center + half, top + 9, withAlpha(GOLD, progress));
        drawCloudCorner(graphics, left + 9, top + 9, 1, withAlpha(GOLD_MUTED, progress));
        drawCloudCorner(graphics, right - 9, top + 9, -1, withAlpha(GOLD_MUTED, progress));
        drawCloudCorner(graphics, left + 9, bottom - 9, 1, withAlpha(GOLD_MUTED, progress));
        drawCloudCorner(graphics, right - 9, bottom - 9, -1, withAlpha(GOLD_MUTED, progress));
    }

    public static void drawBorder(GuiGraphics graphics, int left, int top, int right, int bottom, int color) {
        graphics.fill(left, top, right, top + 1, color);
        graphics.fill(left, bottom - 1, right, bottom, color);
        graphics.fill(left, top, left + 1, bottom, color);
        graphics.fill(right - 1, top, right, bottom, color);
    }

    private static void drawCloudCorner(GuiGraphics graphics, int x, int y, int direction, int color) {
        fillHorizontal(graphics, x, x + direction * 8, y, y + 1, color);
        fillHorizontal(graphics, x + direction * 5, x + direction * 6, y, y + 5, color);
        fillHorizontal(graphics, x + direction * 2, x + direction * 6, y + 4, y + 5, color);
    }

    private static void fillHorizontal(GuiGraphics graphics, int x1, int x2, int y1, int y2, int color) {
        graphics.fill(Math.min(x1, x2), y1, Math.max(x1, x2), y2, color);
    }

    private static int withAlpha(int color, float progress) {
        int alpha = (int) (((color >>> 24) & 0xFF) * Mth.clamp(progress, 0.0F, 1.0F));
        return color & 0x00FFFFFF | alpha << 24;
    }

    private static float easeOut(float value) {
        float inverse = 1.0F - Mth.clamp(value, 0.0F, 1.0F);
        return 1.0F - inverse * inverse * inverse;
    }
}
