package com.p1nero.tudigong.client.screen;

import com.p1nero.tudigong.client.widget.TudiGongUiTheme;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.server.HandleNpcInteractionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Built-in replacement for Dialogue Lib's screen. Its defaults mirror the
 * existing p1nero_dl-client.toml: right-side options, no full background,
 * answer/option strips enabled, and a two-character typewriter step every
 * two ticks.
 */
public final class TudiGongDialogueScreen extends Screen {
    private static final String PREFIX = "entity.tudigong.tudigong.tudigong.";
    private static final int DIALOG_WIDTH = 300;
    private static final int TYPEWRITER_SPEED = 1;
    private static final int TYPEWRITER_INTERVAL = 1;
    private static final boolean OPTION_IN_CENTER = false;
    private static final boolean ENABLE_BACKGROUND = false;
    private static final boolean ENABLE_ANSWER_BACKGROUND = true;
    private static final boolean ENABLE_OPTION_BACKGROUND = true;

    private final int entityId;
    private final boolean fromHurt;
    private final List<DialogueOptionButton> options = new ArrayList<>();
    private String answerText = "";
    private int visibleCharacters;
    private int typewriterTimer;
    private boolean answerComplete;
    private boolean completed;
    private float animationProgress;
    private int answerX;
    private int answerY;
    private List<FormattedCharSequence> answerLines = List.of();

    public TudiGongDialogueScreen(int entityId, boolean fromHurt) {
        super(Component.translatable("entity.tudigong.tudigong"));
        this.entityId = entityId;
        this.fromHurt = fromHurt;
    }

    @Override
    protected void init() {
        this.options.clear();
        this.answerText = "[" + Component.translatable("entity.tudigong.tudigong").getString() + "]:\n"
                + Component.translatable(PREFIX + (this.fromHurt ? "answer0" : "answer1")).getString();
        this.visibleCharacters = 0;
        this.typewriterTimer = 0;
        this.answerComplete = false;
        if (this.fromHurt) {
            addOption("option0", 0);
        } else {
            addOption("option1", 10);
            addOption("option2", 11);
            addOption("option3", 3);
        }
        updateAnswerLayout();
    }

    private void addOption(String key, int interactionId) {
        DialogueOptionButton option = new DialogueOptionButton(
                Component.translatable(PREFIX + key), button -> {
                    if (interactionId == 10) {
                        openSearch(true);
                    } else if (interactionId == 11) {
                        openSearch(false);
                    } else {
                        finish(interactionId);
                    }
                });
        this.options.add(option);
        this.addRenderableWidget(option);
    }

    @Override
    public void tick() {
        super.tick();
        this.animationProgress = Math.min(1.0F, this.animationProgress + 0.1F);
        if (this.answerComplete) {
            return;
        }
        if (this.typewriterTimer > 0) {
            this.typewriterTimer--;
            return;
        }
        this.visibleCharacters = Math.min(this.answerText.length(), this.visibleCharacters + TYPEWRITER_SPEED);
        this.typewriterTimer = TYPEWRITER_INTERVAL;
        this.answerComplete = this.visibleCharacters >= this.answerText.length();
        updateAnswerLayout();
    }

    private void updateAnswerLayout() {
        String visible = this.answerText.substring(0, Math.min(this.visibleCharacters, this.answerText.length()));
        this.answerLines = this.font.split(Component.literal(visible), DIALOG_WIDTH);
        int maxWidth = 0;
        for (FormattedCharSequence line : this.answerLines) {
            maxWidth = Math.max(maxWidth, this.font.width(line));
        }
        this.answerX = this.width / 2 - maxWidth / 2;
        this.answerY = (int) (this.height / 2.0F * 1.4F);

        int optionX = OPTION_IN_CENTER ? this.width / 2 : this.width / 2 + this.width / 6;
        int optionY = this.answerY - this.options.size() * 12;
        for (int i = 0; i < this.options.size(); i++) {
            DialogueOptionButton option = this.options.get(i);
            option.setX(OPTION_IN_CENTER ? optionX - option.getWidth() / 2 : optionX);
            option.setY(optionY + i * 12);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float progress = Mth.clamp(this.animationProgress + partialTick * 0.1F, 0.0F, 1.0F);
        if (ENABLE_BACKGROUND) {
            renderDialogueBackground(graphics, progress);
        }

        int lineY = this.answerY;
        for (FormattedCharSequence line : this.answerLines) {
            int lineWidth = this.font.width(line);
            int lineX = this.width / 2 - lineWidth / 2;
            if (ENABLE_ANSWER_BACKGROUND) {
                graphics.fillGradient(lineX - 3, lineY - 2, lineX + lineWidth + 3, lineY + 10,
                        0x66000000, 0x66000000);
            }
            graphics.drawString(this.font, line, lineX + 1, lineY + 1, TudiGongUiTheme.INK, false);
            lineY += 12;
        }

        for (DialogueOptionButton option : this.options) {
            option.visible = this.answerComplete;
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderDialogueBackground(GuiGraphics graphics, float progress) {
        int posY = this.answerY - 5;
        int gradientHeight = Math.max(1, this.height - posY);
        for (int i = 0; i < gradientHeight; i++) {
            float curve = (float) i / gradientHeight;
            int alpha = (int) (0xA0 * (1.0F - curve * curve) * progress);
            if (alpha > 0) {
                int currentY = this.height - i;
                graphics.fill(0, currentY, this.width, currentY + 1, alpha << 24);
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics p_283688_, int p_296369_, int p_296477_, float p_294317_) {
    }

    private void openSearch(boolean structure) {
        this.completed = true;
        TDGPacketHandler.sendToServer(new HandleNpcInteractionPacket(this.entityId, 2));
        this.minecraft.setScreen(structure ? new StructureSearchScreen(this.entityId) : new BiomeSearchScreen(this.entityId));
    }

    private void finish(int interactionId) {
        this.completed = true;
        TDGPacketHandler.sendToServer(new HandleNpcInteractionPacket(this.entityId, interactionId));
        this.minecraft.setScreen(null);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.answerComplete) {
            this.visibleCharacters = this.answerText.length();
            this.answerComplete = true;
            updateAnswerLayout();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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

    private static final class DialogueOptionButton extends Button {
        private DialogueOptionButton(Component message, OnPress onPress) {
            super(Button.builder(message, onPress).pos(0, 0).size(1, 12).createNarration(DEFAULT_NARRATION));
            this.width = Minecraft.getInstance().font.width(message) + 8;
            this.height = 12;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (ENABLE_OPTION_BACKGROUND || this.isHoveredOrFocused()) {
                graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height,
                        0x66000000, 0x66000000);
            }
            graphics.drawString(Minecraft.getInstance().font, this.getMessage(), this.getX() + 3, this.getY() + 2,
                    TudiGongUiTheme.INK, false);
            if (this.isHoveredOrFocused()) {
                graphics.renderOutline(this.getX(), this.getY(), this.width, this.height, TudiGongUiTheme.GOLD);
            }
        }
    }
}
