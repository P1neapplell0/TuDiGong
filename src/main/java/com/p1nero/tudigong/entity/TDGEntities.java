package com.p1nero.tudigong.entity;

import com.p1nero.tudigong.TuDiGongMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

@EventBusSubscriber(modid = TuDiGongMod.MOD_ID)
public class TDGEntities {

    public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, TuDiGongMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<TudiGongEntity>> TU_DI_GONG = register("tudigong",
                    EntityType.Builder.of(TudiGongEntity::new, MobCategory.CREATURE).sized(0.5f, 1.5f).noSave().fireImmune());
    public static final DeferredHolder<EntityType<?>, EntityType<XianQiEntity>> XIAN_QI = register("xian_qi",
            EntityType.Builder.<XianQiEntity>of(XianQiEntity::new, MobCategory.AMBIENT).sized(1, 1).noSave().fireImmune());

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.Builder<T> entityTypeBuilder) {
        return REGISTRY.register(name, () -> entityTypeBuilder.build(ResourceLocation.fromNamespaceAndPath(TuDiGongMod.MOD_ID, name).toString()));
    }

    @SubscribeEvent
    public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
        event.put(TU_DI_GONG.get(), TudiGongEntity.createAttributes().build());
        event.put(XIAN_QI.get(), XianQiEntity.createAttributes().build());
    }

}
