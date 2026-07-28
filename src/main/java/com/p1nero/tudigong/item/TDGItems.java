package com.p1nero.tudigong.item;

import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.item.custom.TuDiCommandSpellItem;
import com.p1nero.tudigong.item.custom.GeomancyTalismanItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class TDGItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(TuDiGongMod.MOD_ID);
    public static final DeferredItem<Item> TUDI_COMMAND_SPELL = REGISTRY.register("tudi_command_spell", () -> new TuDiCommandSpellItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<Item> GEOMANCY_TALISMAN = REGISTRY.register("geomancy_talisman", () -> new GeomancyTalismanItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

}
