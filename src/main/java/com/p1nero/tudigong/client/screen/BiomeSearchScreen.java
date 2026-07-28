package com.p1nero.tudigong.client.screen;

import com.p1nero.tudigong.client.widget.ResourceList;
import com.p1nero.tudigong.client.widget.TudiGongButton;
import com.p1nero.tudigong.client.widget.TudiGongEditBox;
import com.p1nero.tudigong.client.widget.TudiGongUiTheme;
import com.p1nero.tudigong.network.TDGPacketHandler;
import com.p1nero.tudigong.network.packet.server.HandleSearchPacket;
import com.p1nero.tudigong.network.packet.server.HandleNpcInteractionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class BiomeSearchScreen extends Screen {
    public static final Map<ResourceLocation, String> BIOME_NAME_MAP = new LinkedHashMap<>();
    public static final Map<String, Set<ResourceLocation>> BIOME_MOD_IDS = new LinkedHashMap<>();
    public static final Map<ResourceLocation, List<ResourceLocation>> BIOME_DIMENSIONS = new LinkedHashMap<>();
    private static long dataVersion;

    private final int tudigongId;
    private EditBox searchBox;
    private ResourceList resourceList;
    private TudiGongButton searchButton;
    private boolean found;
    private long observedDataVersion = -1;
    private int resultCount;
    private float animationProgress;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;

    public BiomeSearchScreen(int tudigongId) {
        super(Component.translatable("gui.tudigong.search.biome_title"));
        this.tudigongId = tudigongId;
    }

    public static void markDataChanged() {
        dataVersion++;
    }

    @Override
    protected void init() {
        super.init();
        String previousQuery = this.searchBox == null ? "" : this.searchBox.getValue();
        int panelWidth = Math.min(410, this.width - 24);
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelRight = this.panelLeft + panelWidth;
        this.panelTop = 12;
        this.panelBottom = this.height - 12;
        int contentLeft = this.panelLeft + 12;
        int contentWidth = panelWidth - 24;
        int buttonWidth = 82;
        int fieldTop = this.panelTop + 31;
        int listTop = fieldTop + 27;
        int listBottom = this.panelBottom - 34;

        this.searchBox = new TudiGongEditBox(this.font, contentLeft, fieldTop, contentWidth - buttonWidth - 6, 21,
                Component.translatable("gui.tudigong.search.placeholder"));
        this.searchBox.setMaxLength(32500);
        this.searchBox.setValue(previousQuery);
        this.addRenderableWidget(this.searchBox);
        this.searchButton = new TudiGongButton(contentLeft + contentWidth - buttonWidth, fieldTop, buttonWidth, 21,
                Component.translatable("button.tudigong.ask"), this::onSearchButtonPressed);
        this.addRenderableWidget(this.searchButton);
        this.addRenderableWidget(new TudiGongButton(contentLeft, this.panelBottom - 27, 86, 19,
                Component.translatable("gui.tudigong.history.button"),
                button -> this.minecraft.setScreen(new HistoryScreen(this))));

        this.resourceList = new ResourceList(Minecraft.getInstance(), contentWidth, this.height, listTop, listBottom, 32,
                BIOME_NAME_MAP, this.searchBox, null, BIOME_MOD_IDS, null, BIOME_DIMENSIONS, null, null,
                count -> this.resultCount = count);
        this.resourceList.setX(contentLeft);
        this.addRenderableWidget(this.resourceList);
        this.searchBox.setResponder(this.resourceList::refresh);
        this.resourceList.refresh(previousQuery, false);
        this.observedDataVersion = dataVersion;
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public void tick() {
        super.tick();
        this.animationProgress = Math.min(1.0F, this.animationProgress + 0.1F);
        if (this.observedDataVersion != dataVersion && this.resourceList != null) {
            this.observedDataVersion = dataVersion;
            this.resourceList.refresh(this.searchBox.getValue(), true);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float progress = Mth.clamp(this.animationProgress + partialTick * 0.1F, 0.0F, 1.0F);
        TudiGongUiTheme.renderBackdrop(graphics, this.width, this.height, this.panelLeft, this.panelTop, this.panelRight, this.panelBottom, progress);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.panelTop + 15, TudiGongUiTheme.INK);
        graphics.drawCenteredString(this.font, Component.translatable("gui.tudigong.search.result_count", this.resultCount),
                this.width / 2, this.panelBottom - 23, 0xFFB7A278);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void onSearchButtonPressed(net.minecraft.client.gui.components.Button button) {
        String searchString = this.searchBox.getValue().trim();
        if (searchString.isEmpty()) {
            return;
        }
        ResourceLocation selected = this.resourceList.getSelectedResourceIdForCurrentInput();
        String searchToSend = selected == null ? searchString : selected.toString();
        TDGPacketHandler.sendToServer(new HandleSearchPacket(this.tudigongId, searchToSend, false));
        this.found = true;
        this.onClose();
    }

    @Override
    public void onClose() {
        super.onClose();
        if (!this.found) {
            TDGPacketHandler.sendToServer(new HandleNpcInteractionPacket(this.tudigongId, 0));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.resourceList.page(keyCode == GLFW.GLFW_KEY_PAGE_UP ? -1 : 1);
            return true;
        }
        if (this.searchBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_TAB) {
                this.resourceList.handleTabCompletion();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.onSearchButtonPressed(this.searchButton);
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public int getTudigongId() {
        return this.tudigongId;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
