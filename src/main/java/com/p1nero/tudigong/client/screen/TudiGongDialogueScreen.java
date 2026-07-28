package com.p1nero.tudigong.client.screen;

import com.p1nero.tudigong.client.widget.TudiGongButton;
import com.p1nero.tudigong.client.widget.TudiGongUiTheme;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.server.HandleNpcInteractionPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public final class TudiGongDialogueScreen extends Screen {
    private static final String PREFIX = "entity.tudigong.tudigong.tudigong.";
    private final int entityId;
    private final boolean fromHurt;
    private boolean completed;
    private float animationProgress;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;

    public TudiGongDialogueScreen(int entityId, boolean fromHurt) {
        super(Component.translatable("entity.tudigong.tudigong"));
        this.entityId = entityId;
        this.fromHurt = fromHurt;
    }

    @Override
    protected void init() {
        int panelWidth = Math.min(360, this.width - 24);
        int optionCount = this.fromHurt ? 1 : 3;
        int panelHeight = 70 + optionCount * 25;
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelRight = this.panelLeft + panelWidth;
        this.panelBottom = this.height - 24;
        this.panelTop = this.panelBottom - panelHeight;

        int buttonX = this.panelLeft + 16;
        int buttonWidth = panelWidth - 32;
        int buttonY = this.panelTop + 52;
        if (this.fromHurt) {
            this.addRenderableWidget(new TudiGongButton(buttonX, buttonY, buttonWidth, 20,
                    Component.translatable(PREFIX + "option0"), button -> finish(0)));
            return;
        }

        this.addRenderableWidget(new TudiGongButton(buttonX, buttonY, buttonWidth, 20,
                Component.translatable(PREFIX + "option1"), button -> openStructureSearch()));
        this.addRenderableWidget(new TudiGongButton(buttonX, buttonY + 25, buttonWidth, 20,
                Component.translatable(PREFIX + "option2"), button -> openBiomeSearch()));
        this.addRenderableWidget(new TudiGongButton(buttonX, buttonY + 50, buttonWidth, 20,
                Component.translatable(PREFIX + "option3"), button -> finish(3)));
    }

    @Override
    public void tick() {
        this.animationProgress = Math.min(1.0F, this.animationProgress + 0.1F);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float progress = Mth.clamp(this.animationProgress + partialTick * 0.1F, 0.0F, 1.0F);
        TudiGongUiTheme.renderBackdrop(graphics, this.width, this.height, this.panelLeft, this.panelTop,
                this.panelRight, this.panelBottom, progress);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelTop + 15, TudiGongUiTheme.GOLD);
        graphics.drawCenteredString(this.font, Component.translatable(PREFIX + (this.fromHurt ? "answer0" : "answer1")),
                this.width / 2, this.panelTop + 33, TudiGongUiTheme.INK);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void openStructureSearch() {
        this.completed = true;
        TDGPacketHandler.sendToServer(new HandleNpcInteractionPacket(this.entityId, 2));
        this.minecraft.setScreen(new StructureSearchScreen(this.entityId));
    }

    private void openBiomeSearch() {
        this.completed = true;
        TDGPacketHandler.sendToServer(new HandleNpcInteractionPacket(this.entityId, 2));
        this.minecraft.setScreen(new BiomeSearchScreen(this.entityId));
    }

    private void finish(int interactionId) {
        this.completed = true;
        TDGPacketHandler.sendToServer(new HandleNpcInteractionPacket(this.entityId, interactionId));
        this.minecraft.setScreen(null);
    }

    @Override
    public void onClose() {
        if (!this.completed) {
            TDGPacketHandler.sendToServer(new HandleNpcInteractionPacket(this.entityId, 0));
        }
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
