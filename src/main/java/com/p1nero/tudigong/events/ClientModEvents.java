package com.p1nero.tudigong.events;

import com.p1nero.tudigong.TuDiGongMod;
import com.p1nero.tudigong.client.util.SearchHistoryManager;
import com.p1nero.tudigong.entity.TDGEntities;
import com.p1nero.tudigong.entity.client.TudiGongModel;
import com.p1nero.tudigong.entity.client.TudiGongRenderer;
import com.p1nero.tudigong.entity.client.XianQiRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = TuDiGongMod.MOD_ID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        EntityRenderers.register(TDGEntities.TU_DI_GONG.get(), TudiGongRenderer::new);
        EntityRenderers.register(TDGEntities.XIAN_QI.get(), XianQiRenderer::new);
        event.enqueueWork(SearchHistoryManager::load);
    }

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TudiGongModel.LAYER_LOCATION, TudiGongModel::createBodyLayer);
    }
}
