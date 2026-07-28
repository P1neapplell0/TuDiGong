package com.p1nero.tudigong.item.custom;

import com.p1nero.tudigong.util.TextUtil;
import com.p1nero.tudigong.util.BiomeUtil;
import com.p1nero.tudigong.util.StructureUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
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
        ItemStack stack = new ItemStack(com.p1nero.tudigong.item.TDGItems.GEOMANCY_TALISMAN.get());
        CompoundTag tag = new CompoundTag();
        tag.putString(TARGET_NAME, targetId);
        tag.putInt(TARGET_X, target.getX());
        tag.putInt(TARGET_Y, target.getY());
        tag.putInt(TARGET_Z, target.getZ());
        tag.putString(TARGET_DIMENSION, dimension.location().toString());
        tag.putString(TARGET_DIRECTION, directionKey);
        tag.putBoolean(TARGET_IS_STRUCTURE, isStructure);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
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
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
        if (!tag.contains(TARGET_X) || !tag.contains(TARGET_DIMENSION)) {
            return;
        }
        ResourceKey<Level> dimension = ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                net.minecraft.resources.ResourceLocation.parse(tag.getString(TARGET_DIMENSION)));
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).getUnsafe();
        if (tag.contains(TARGET_NAME)) {
            tooltip.add(Component.translatable("item.tudigong.geomancy_talisman.target",
                    getTargetName(tag)).withStyle(ChatFormatting.GOLD));
            if (tag.contains(TARGET_DIRECTION)) {
                tooltip.add(Component.translatable("item.tudigong.geomancy_talisman.direction",
                        Component.translatable(tag.getString(TARGET_DIRECTION))).withStyle(ChatFormatting.YELLOW));
            }
            tooltip.add(Component.translatable("item.tudigong.geomancy_talisman.position",
                    tag.getInt(TARGET_X), tag.getInt(TARGET_Y), tag.getInt(TARGET_Z)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.tudigong.geomancy_talisman.dimension",
                    TextUtil.getDimensionName(ResourceLocation.parse(tag.getString(TARGET_DIMENSION)))).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static String getTargetName(CompoundTag tag) {
        ResourceLocation targetId = ResourceLocation.tryParse(tag.getString(TARGET_NAME));
        if (targetId == null) {
            return tag.getString(TARGET_NAME);
        }
        return tag.getBoolean(TARGET_IS_STRUCTURE)
                ? StructureUtils.getPrettyStructureName(targetId)
                : BiomeUtil.getBiomeName(targetId);
    }
}
