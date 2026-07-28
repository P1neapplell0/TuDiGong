package com.p1nero.tudigong.client.widget;

import com.p1nero.tudigong.client.util.SearchHistoryManager;
import com.p1nero.tudigong.client.util.SearchHistoryManager.SearchHistoryEntry;
import com.p1nero.tudigong.compat.JECharactersIntegration;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.server.TeleportToServerPacket;
import com.p1nero.tudigong.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Stream;

public class HistoryList extends ObjectSelectionList<HistoryList.Entry> {
    private String currentFilter = "";
    private int resultCount;

    public HistoryList(Minecraft minecraft, int width, int height, int y0, int y1) {
        super(minecraft, width, y1 - y0, y0, 48);
    }

    public void filter(String keyword) {
        this.currentFilter = keyword == null ? "" : keyword;
        this.clearEntries();
        Stream<SearchHistoryEntry> stream = SearchHistoryManager.getHistory().stream();
        if (!StringUtil.isNullOrEmpty(this.currentFilter)) {
            String normalized = this.currentFilter.toLowerCase(Locale.ROOT);
            stream = stream.filter(entry -> matches(entry, normalized));
        }
        stream.forEach(entry -> this.addEntry(new Entry(this, entry)));
        this.resultCount = this.children().size();
        this.setScrollAmount(0);
    }

    private boolean matches(SearchHistoryEntry entry, String keyword) {
        String translatedName = TextUtil.tryToGetName(entry.searchTerm());
        if (JECharactersIntegration.match(translatedName, keyword)
                || JECharactersIntegration.match(entry.searchTerm(), keyword)
                || JECharactersIntegration.match(entry.type().getString(), keyword)) {
            return true;
        }
        if (entry.position() != null) {
            String position = entry.position().getX() + ", " + entry.position().getY() + ", " + entry.position().getZ();
            if (position.contains(keyword)) {
                return true;
            }
        }
        if (entry.dimension() != null && entry.dimension().location().toString().toLowerCase(Locale.ROOT).contains(keyword)) {
            return true;
        }
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(entry.timestamp()));
        return timestamp.contains(keyword);
    }

    public int getResultCount() {
        return this.resultCount;
    }

    @Override
    protected void renderListBackground(@NotNull GuiGraphics graphics) {
        graphics.fill(this.getX(), this.getY(), this.getRight(), this.getBottom(), 0xC812100F);
        TudiGongUiTheme.drawBorder(graphics, this.getX(), this.getY(), this.getRight(), this.getBottom(), TudiGongUiTheme.GOLD_MUTED);
    }

    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.getRight() - 6;
    }

    @Override
    protected void renderDecorations(GuiGraphics graphics, int mouseX, int mouseY) {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) {
            return;
        }
        int scrollbarX = this.getScrollbarPosition();
        int viewportHeight = this.getHeight();
        int thumbHeight = Mth.clamp((int) ((float) (viewportHeight * viewportHeight) / this.getMaxPosition()),
                24, viewportHeight - 8);
        int thumbTop = (int) this.getScrollAmount() * (viewportHeight - thumbHeight) / maxScroll + this.getY();
        graphics.fill(scrollbarX, this.getY(), scrollbarX + 6, this.getBottom(), 0xFF171310);
        graphics.fill(scrollbarX + 1, thumbTop, scrollbarX + 5, thumbTop + thumbHeight, TudiGongUiTheme.GOLD_MUTED);
        graphics.fill(scrollbarX + 2, thumbTop + 1, scrollbarX + 4, thumbTop + thumbHeight - 1, TudiGongUiTheme.GOLD);
    }

    @Override
    protected void renderSelection(GuiGraphics graphics, int top, int width, int height, int outerColor, int innerColor) {
    }

    public static class Entry extends ObjectSelectionList.Entry<HistoryList.Entry> {
        private final HistoryList parentList;
        private final SearchHistoryEntry historyEntry;
        private final TudiGongButton deleteButton;
        private final TudiGongButton teleportButton;
        private final Minecraft minecraft;
        private final ItemStack icon;

        public Entry(HistoryList parentList, SearchHistoryEntry historyEntry) {
            this.parentList = parentList;
            this.historyEntry = historyEntry;
            this.minecraft = Minecraft.getInstance();
            this.deleteButton = new TudiGongButton(0, 0, 22, 20, Component.literal("x"), button -> {
                SearchHistoryManager.remove(historyEntry);
                this.parentList.filter(this.parentList.currentFilter);
            });
            this.teleportButton = new TudiGongButton(0, 0, 42, 20,
                    Component.translatable("gui.tudigong.history.teleport"), button -> {
                if (historyEntry.position() != null && historyEntry.dimension() != null) {
                    TDGPacketHandler.sendToServer(new TeleportToServerPacket(historyEntry.position(), historyEntry.dimension()));
                }
            });
            this.teleportButton.visible = historyEntry.position() != null && historyEntry.dimension() != null;
            boolean canTeleport = this.minecraft.player != null && this.minecraft.player.hasPermissions(2);
            this.teleportButton.active = canTeleport;
            if (!canTeleport) {
                this.teleportButton.setTooltip(Tooltip.create(Component.translatable("error.tudigong.teleport_permission")));
            }
            boolean isStructure = historyEntry.type().getString().equalsIgnoreCase(
                    Component.translatable("history.tudigong.type.structure").getString());
            this.icon = new ItemStack(isStructure ? Items.COMPASS : Items.GRASS_BLOCK);
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            int background = isMouseOver ? 0xB43A2A21 : (index & 1) == 0 ? 0x781D1916 : 0x70241E19;
            graphics.fill(left, top, left + width, top + height, background);
            if (isMouseOver) {
                graphics.fill(left, top, left + 2, top + height, TudiGongUiTheme.GOLD_MUTED);
            }
            graphics.renderFakeItem(this.icon, left + 8, top + 15);

            int buttonsWidth = this.teleportButton.visible ? 74 : 28;
            int availableTextWidth = Math.max(30, width - 42 - buttonsWidth);
            String translatedName = TextUtil.tryToGetName(this.historyEntry.searchTerm());
            translatedName = this.minecraft.font.plainSubstrByWidth(translatedName, availableTextWidth);
            graphics.drawString(this.minecraft.font, translatedName, left + 34, top + 7, TudiGongUiTheme.INK, false);

            String position = Component.translatable("gui.tudigong.history.pos_na").getString();
            if (this.historyEntry.position() != null) {
                BlockPos pos = this.historyEntry.position();
                String y = pos.getY() == -1145 ? "~" : String.valueOf(pos.getY());
                position = pos.getX() + ", " + y + ", " + pos.getZ();
                if (this.historyEntry.dimension() != null) {
                    position += " (" + this.historyEntry.dimension().location().getPath() + ")";
                }
            }
            position = this.minecraft.font.plainSubstrByWidth(position, availableTextWidth);
            graphics.drawString(this.minecraft.font, position, left + 34, top + 20, 0xFF8FC28A, false);

            String timestamp = new SimpleDateFormat("yy/MM/dd HH:mm").format(new Date(this.historyEntry.timestamp()));
            graphics.drawString(this.minecraft.font, timestamp, left + 34, top + 33, 0xFF8D857B, false);

            this.deleteButton.setX(left + width - this.deleteButton.getWidth() - 5);
            this.deleteButton.setY(top + (height - this.deleteButton.getHeight()) / 2);
            this.deleteButton.render(graphics, mouseX, mouseY, partialTicks);
            if (this.teleportButton.visible) {
                this.teleportButton.setX(this.deleteButton.getX() - this.teleportButton.getWidth() - 5);
                this.teleportButton.setY(top + (height - this.teleportButton.getHeight()) / 2);
                this.teleportButton.render(graphics, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (this.teleportButton.visible && this.teleportButton.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            return this.deleteButton.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(this.historyEntry.searchTerm());
        }
    }
}
