package com.p1nero.tudigong.client.screen;

import com.p1nero.tudigong.client.util.SearchHistoryManager;
import com.p1nero.tudigong.client.widget.HistoryList;
import com.p1nero.tudigong.client.widget.TudiGongButton;
import com.p1nero.tudigong.client.widget.TudiGongEditBox;
import com.p1nero.tudigong.client.widget.TudiGongUiTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HistoryScreen extends Screen {
    private HistoryList historyList;
    private final Screen parentScreen;
    private EditBox searchBox;
    private float animationProgress;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;

    public HistoryScreen(Screen parentScreen) {
        super(Component.translatable("gui.tudigong.history.title"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        String previousQuery = this.searchBox == null ? "" : this.searchBox.getValue();
        int panelWidth = Math.min(480, this.width - 20);
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelRight = this.panelLeft + panelWidth;
        this.panelTop = 10;
        this.panelBottom = this.height - 10;
        int contentLeft = this.panelLeft + 12;
        int contentWidth = panelWidth - 24;
        int fieldTop = this.panelTop + 31;
        int listTop = fieldTop + 27;
        int listBottom = this.panelBottom - 36;

        this.searchBox = new TudiGongEditBox(this.font, contentLeft, fieldTop, contentWidth, 21,
                Component.translatable("gui.tudigong.history.search_placeholder"));
        this.searchBox.setValue(previousQuery);
        this.addRenderableWidget(this.searchBox);

        this.historyList = new HistoryList(this.minecraft, contentWidth, this.height, listTop, listBottom);
        this.historyList.setX(contentLeft);
        this.addRenderableWidget(this.historyList);
        this.searchBox.setResponder(this.historyList::filter);
        this.historyList.filter(previousQuery);

        int buttonWidth = 96;
        this.addRenderableWidget(new TudiGongButton(contentLeft, this.panelBottom - 28, buttonWidth, 20,
                Component.translatable("gui.tudigong.history.clear"), button -> {
            SearchHistoryManager.clearHistory();
            this.searchBox.setValue("");
            this.historyList.filter("");
        }));
        this.addRenderableWidget(new TudiGongButton(this.panelRight - 12 - buttonWidth, this.panelBottom - 28,
                buttonWidth, 20, Component.translatable("gui.done"), button -> this.onClose()));
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public void tick() {
        super.tick();
        this.animationProgress = Math.min(1.0F, this.animationProgress + 0.1F);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float progress = Mth.clamp(this.animationProgress + partialTick * 0.1F, 0.0F, 1.0F);
        TudiGongUiTheme.renderBackdrop(graphics, this.width, this.height, this.panelLeft, this.panelTop, this.panelRight, this.panelBottom, progress);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelTop + 15, TudiGongUiTheme.INK);
        graphics.drawCenteredString(this.font, Component.translatable("gui.tudigong.search.result_count", this.historyList.getResultCount()),
                this.width / 2, this.panelBottom - 23, 0xFFB7A278);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parentScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void renderBlurredBackground(float p_330683_) {

    }
}
