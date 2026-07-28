package com.p1nero.tudigong.client.widget;

import com.p1nero.tudigong.compat.JECharactersIntegration;
import com.p1nero.tudigong.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ResourceList extends ObjectSelectionList<ResourceList.Entry> {
    private final Map<ResourceLocation, String> names;
    private final Map<String, Set<ResourceLocation>> tags;
    private final Map<String, Set<ResourceLocation>> modIds;
    private final Map<String, Set<ResourceLocation>> sets;
    private final Map<String, Set<ResourceLocation>> types;
    private final Map<ResourceLocation, ResourceLocation> structureToTypeMap;
    private final Map<ResourceLocation, List<ResourceLocation>> dimensions;
    private final EditBox box;
    private final Map<ResourceLocation, Set<String>> resourceToTagsMap = new HashMap<>();
    private final IntConsumer resultCountListener;
    private boolean updatingBox;

    public ResourceList(Minecraft minecraft, int width, int height, int y0, int y1, int itemHeight,
                        Map<ResourceLocation, String> names, EditBox box,
                        Map<String, Set<ResourceLocation>> tags, Map<String, Set<ResourceLocation>> modIds,
                        Map<String, Set<ResourceLocation>> sets,
                        Map<ResourceLocation, List<ResourceLocation>> dimensions,
                        Map<String, Set<ResourceLocation>> types,
                        Map<ResourceLocation, ResourceLocation> structureToTypeMap,
                        IntConsumer resultCountListener) {
        super(minecraft, width, height, y0, y1, itemHeight);
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
        this.setRenderSelection(false);
        this.names = names;
        this.box = box;
        this.tags = tags;
        this.modIds = modIds;
        this.sets = sets;
        this.dimensions = dimensions;
        this.types = types;
        this.structureToTypeMap = structureToTypeMap;
        this.resultCountListener = resultCountListener;
    }

    @Override
    protected void renderBackground(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.fill(this.x0, this.y0, this.x1, this.y1, 0xC812100F);
        TudiGongUiTheme.drawBorder(guiGraphics, this.x0, this.y0, this.x1, this.y1, TudiGongUiTheme.GOLD_MUTED);
    }

    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    @Override
    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    @Override
    protected void renderDecorations(GuiGraphics graphics, int mouseX, int mouseY) {
        int maxScroll = this.getMaxScroll();
        if (maxScroll <= 0) {
            return;
        }
        int scrollbarX = this.getScrollbarPosition();
        int viewportHeight = this.y1 - this.y0;
        int thumbHeight = Mth.clamp((int) ((float) (viewportHeight * viewportHeight) / this.getMaxPosition()),
                24, viewportHeight - 8);
        int thumbTop = (int) this.getScrollAmount() * (viewportHeight - thumbHeight) / maxScroll + this.y0;
        graphics.fill(scrollbarX, this.y0, scrollbarX + 6, this.y1, 0xFF171310);
        graphics.fill(scrollbarX + 1, thumbTop, scrollbarX + 5, thumbTop + thumbHeight, TudiGongUiTheme.GOLD_MUTED);
        graphics.fill(scrollbarX + 2, thumbTop + 1, scrollbarX + 4, thumbTop + thumbHeight - 1, TudiGongUiTheme.GOLD);
    }

    @Override
    public void setSelected(@Nullable Entry entry) {
        super.setSelected(entry);
        if (entry != null) {
            this.updatingBox = true;
            this.box.setValue(entry.displayName);
            this.updatingBox = false;
        }
    }

    @Nullable
    public ResourceLocation getSelectedResourceId() {
        Entry selected = this.getSelected();
        return selected == null ? null : selected.resourceId;
    }

    @Nullable
    public ResourceLocation getSelectedResourceIdForCurrentInput() {
        Entry selected = this.getSelected();
        String input = this.box.getValue().trim();
        if (selected != null && selected.displayName.equals(input)) {
            return selected.resourceId;
        }
        return this.names.entrySet().stream()
                .filter(entry -> entry.getValue().equals(input))
                .map(Map.Entry::getKey)
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .findFirst()
                .orElse(null);
    }

    public void refresh(String keyword) {
        refresh(keyword, true);
    }

    public void refresh(String keyword, boolean maintainSelection) {
        if (this.updatingBox) {
            return;
        }

        ResourceLocation selectedId = maintainSelection ? this.getSelectedResourceId() : null;
        this.rebuildReverseTagMap();
        List<ResourceLocation> results = StringUtil.isNullOrEmpty(keyword)
                ? new ArrayList<>(this.names.keySet())
                : this.findMatches(keyword.trim());
        results.sort(Comparator
                .comparing((ResourceLocation id) -> this.names.getOrDefault(id, id.toString()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(ResourceLocation::toString));

        this.clearEntries();
        Entry selectedEntry = null;
        for (ResourceLocation resourceId : results) {
            Entry entry = new Entry(resourceId, this.names.getOrDefault(resourceId, resourceId.toString()));
            this.addEntry(entry);
            if (resourceId.equals(selectedId)) {
                selectedEntry = entry;
            }
        }
        super.setSelected(selectedEntry);
        this.setScrollAmount(0.0D);
        this.resultCountListener.accept(results.size());
    }

    private void rebuildReverseTagMap() {
        this.resourceToTagsMap.clear();
        if (this.tags != null) {
            this.tags.forEach((tagName, resources) -> resources.forEach(resource ->
                    this.resourceToTagsMap.computeIfAbsent(resource, ignored -> new HashSet<>()).add(tagName)));
        }
    }

    private List<ResourceLocation> findMatches(String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase(Locale.ROOT);
        if (!keyword.isEmpty() && "#@$~".indexOf(keyword.charAt(0)) >= 0) {
            Map<String, Set<ResourceLocation>> lookupMap = lookupForPrefix(keyword.charAt(0));
            if (lookupMap == null) {
                return Collections.emptyList();
            }
            String term = keyword.substring(1).toLowerCase(Locale.ROOT);
            Set<ResourceLocation> matches = lookupMap.entrySet().stream()
                    .filter(entry -> JECharactersIntegration.match(entry.getKey(), term))
                    .flatMap(entry -> entry.getValue().stream())
                    .filter(this.names::containsKey)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            return new ArrayList<>(matches);
        }

        List<ResourceLocation> matches = new ArrayList<>();
        this.names.forEach((id, name) -> {
            if (JECharactersIntegration.match(name, lowerCaseKeyword)
                    || JECharactersIntegration.match(id.toString(), lowerCaseKeyword)
                    || this.matchesTags(id, lowerCaseKeyword)) {
                matches.add(id);
            }
        });
        return matches;
    }

    private boolean matchesTags(ResourceLocation id, String keyword) {
        Set<String> associatedTags = this.resourceToTagsMap.get(id);
        return associatedTags != null && associatedTags.stream().anyMatch(tag -> JECharactersIntegration.match(tag, keyword));
    }

    @Nullable
    private Map<String, Set<ResourceLocation>> lookupForPrefix(char prefix) {
        return switch (prefix) {
            case '#' -> this.tags;
            case '@' -> this.modIds;
            case '$' -> this.sets;
            case '~' -> this.types;
            default -> null;
        };
    }

    public void handleTabCompletion() {
        String keyword = this.box.getValue();
        if (keyword.length() < 2 || "#@$~".indexOf(keyword.charAt(0)) < 0) {
            return;
        }
        char prefix = keyword.charAt(0);
        String term = keyword.substring(1).toLowerCase(Locale.ROOT);
        Map<String, Set<ResourceLocation>> lookupMap = lookupForPrefix(prefix);
        if (lookupMap == null) {
            return;
        }

        List<String> completions = lookupMap.keySet().stream()
                .filter(key -> key.toLowerCase(Locale.ROOT).startsWith(term))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        if (completions.size() == 1) {
            this.box.setValue(prefix + completions.get(0));
        } else if (!completions.isEmpty()) {
            String commonPrefix = StringUtils.getCommonPrefix(completions.toArray(new String[0]));
            if (!commonPrefix.isEmpty()) {
                this.box.setValue(prefix + commonPrefix);
            }
        }
    }

    public void page(int direction) {
        int viewportHeight = Math.max(1, this.y1 - this.y0 - 8);
        this.setScrollAmount(this.getScrollAmount() + direction * viewportHeight);
    }

    @OnlyIn(Dist.CLIENT)
    public class Entry extends ObjectSelectionList.Entry<Entry> {
        private final ResourceLocation resourceId;
        private final String displayName;

        private Entry(ResourceLocation resourceId, String displayName) {
            this.resourceId = resourceId;
            this.displayName = displayName;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            boolean selected = ResourceList.this.getSelected() == this;
            int background = selected ? 0xD05B2525 : isMouseOver ? 0xB43A2A21 : (index & 1) == 0 ? 0x781D1916 : 0x70241E19;
            graphics.fill(left, top, left + width, top + height, background);
            if (selected || isMouseOver) {
                graphics.fill(left, top, left + 2, top + height, selected ? TudiGongUiTheme.GOLD : TudiGongUiTheme.GOLD_MUTED);
            }

            int textWidth = width - 14;
            String name = minecraft.font.plainSubstrByWidth(this.displayName, textWidth);
            graphics.drawString(minecraft.font, name, left + 6, top + 4, TudiGongUiTheme.INK, false);

            int y = top + 16;
            if (ResourceList.this.structureToTypeMap != null) {
                ResourceLocation typeKey = ResourceList.this.structureToTypeMap.get(this.resourceId);
                if (typeKey != null && !typeKey.getPath().equals("none")) {
                    drawClipped(graphics, Component.translatable("gui.tudigong.search.type", typeKey), left + 6, y, textWidth, 0xFFB7A278);
                    y += 11;
                }
            }

            List<ResourceLocation> dims = ResourceList.this.dimensions == null ? null : ResourceList.this.dimensions.get(this.resourceId);
            if (dims != null && !dims.isEmpty() && y + minecraft.font.lineHeight <= top + height) {
                String dimensionNames = dims.stream().map(TextUtil::getDimensionName).collect(Collectors.joining(", "));
                drawClipped(graphics, Component.translatable("gui.tudigong.search.dimensions", dimensionNames), left + 6, y, textWidth, 0xFF918A7D);
            }
        }

        private void drawClipped(GuiGraphics graphics, Component component, int x, int y, int width, int color) {
            String text = component.getString();
            if (minecraft.font.width(text) > width) {
                text = minecraft.font.plainSubstrByWidth(text, Math.max(0, width - minecraft.font.width("..."))) + "...";
            }
            graphics.drawString(minecraft.font, text, x, y, color, false);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.literal(this.displayName).append(" ").append(this.resourceId.toString());
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                ResourceList.this.setSelected(this);
                return true;
            }
            return false;
        }
    }
}
