package com.p1nero.tudigong.item.custom;

import com.p1nero.tudigong.item.TDGItems;
import com.p1nero.tudigong.util.BiomeUtil;
import com.p1nero.tudigong.util.StructureUtils;
import com.p1nero.tudigong.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GeomancyTalismanItem extends Item {
    private static final String TARGET_NAME = "TargetName";
    private static final String TARGET_X = "TargetX";
    private static final String TARGET_Y = "TargetY";
    private static final String TARGET_Z = "TargetZ";
    private static final String TARGET_DIMENSION = "TargetDimension";
    private static final String TARGET_DIRECTION = "TargetDirection";
    private static final String TARGET_IS_STRUCTURE = "TargetIsStructure";

    public GeomancyTalismanItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack create(String targetId, net.minecraft.core.BlockPos target, ResourceKey<Level> dimension,
                                   String directionKey, boolean isStructure) {
        ItemStack stack = new ItemStack(TDGItems.GEOMANCY_TALISMAN.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TARGET_NAME, targetId);
        tag.putInt(TARGET_X, target.getX());
        tag.putInt(TARGET_Y, target.getY());
        tag.putInt(TARGET_Z, target.getZ());
        tag.putString(TARGET_DIMENSION, dimension.location().toString());
        tag.putString(TARGET_DIRECTION, directionKey);
        tag.putBoolean(TARGET_IS_STRUCTURE, isStructure);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player) || level.isClientSide || (!selected && player.getOffhandItem() != stack)) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel) || level.getGameTime() % 6 != 0) {
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TARGET_X) || !tag.contains(TARGET_DIMENSION)) {
            return;
        }
        ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString(TARGET_DIMENSION));
        if (dimensionId == null) {
            return;
        }
        ResourceKey<Level> dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, dimensionId);
        if (!player.level().dimension().equals(dimension)) {
            return;
        }
        Vec3 target = new Vec3(tag.getInt(TARGET_X) + 0.5, tag.getInt(TARGET_Y) + 0.5, tag.getInt(TARGET_Z) + 0.5);
        Vec3 offset = target.subtract(player.getEyePosition());
        if (offset.lengthSqr() < 1.0) {
            return;
        }
        Vec3 direction = offset.normalize();
        double trailDistance = 1.05 + (level.getGameTime() % 18 / 6) * 0.42;
        Vec3 origin = player.getEyePosition().add(direction.scale(trailDistance));
        serverLevel.sendParticles(ParticleTypes.END_ROD, origin.x, origin.y, origin.z, 1, 0.015, 0.015, 0.015, 0.01);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TARGET_NAME)) {
            return;
        }
        ResourceLocation targetId = ResourceLocation.tryParse(tag.getString(TARGET_NAME));
        String targetName = targetId == null ? tag.getString(TARGET_NAME) : tag.getBoolean(TARGET_IS_STRUCTURE)
                ? StructureUtils.getPrettyStructureName(targetId) : BiomeUtil.getBiomeName(targetId);
        tooltip.add(Component.translatable("item.tudigong.geomancy_talisman.target", targetName).withStyle(ChatFormatting.GOLD));
        if (tag.contains(TARGET_DIRECTION)) {
            tooltip.add(Component.translatable("item.tudigong.geomancy_talisman.direction",
                    Component.translatable(tag.getString(TARGET_DIRECTION))).withStyle(ChatFormatting.YELLOW));
        }
        tooltip.add(Component.translatable("item.tudigong.geomancy_talisman.position",
                tag.getInt(TARGET_X), tag.getInt(TARGET_Y), tag.getInt(TARGET_Z)).withStyle(ChatFormatting.GRAY));
        ResourceLocation dimensionId = ResourceLocation.tryParse(tag.getString(TARGET_DIMENSION));
        tooltip.add(Component.translatable("item.tudigong.geomancy_talisman.dimension",
                dimensionId == null ? tag.getString(TARGET_DIMENSION) : TextUtil.getDimensionName(dimensionId)).withStyle(ChatFormatting.DARK_GRAY));
    }
}
