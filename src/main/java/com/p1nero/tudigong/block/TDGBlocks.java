package com.p1nero.tudigong.block;

import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.block.custom.TuDiTempleBlock;
import com.p1nero.tudigong.item.TDGItems;
import com.p1nero.tudigong.item.custom.SimpleDescriptionBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class TDGBlocks {
    public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(TuDiGongMod.MOD_ID);

    public static final DeferredBlock<Block> TUDI_TEMPLE = registerBlock("tudi_temple",
            () -> new TuDiTempleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).noOcclusion()));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = REGISTRY.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        TDGItems.REGISTRY.register(name, () -> new SimpleDescriptionBlockItem(block.get(), new Item.Properties()));
    }

}
