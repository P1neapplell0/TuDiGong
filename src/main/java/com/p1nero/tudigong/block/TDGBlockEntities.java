package com.p1nero.tudigong.block;

import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.block.custom.TuDiTempleBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TDGBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TuDiGongMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TuDiTempleBlockEntity>> TUDI_TEMPLE_ENTITY =
            REGISTRY.register("tudi_temple_entity", () ->
                    BlockEntityType.Builder.of(TuDiTempleBlockEntity::new, TDGBlocks.TUDI_TEMPLE.get()).build(null));
}
