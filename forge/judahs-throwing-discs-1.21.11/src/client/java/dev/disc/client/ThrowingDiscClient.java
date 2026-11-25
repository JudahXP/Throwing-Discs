package dev.disc.client;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = dev.disc.ThrowingDiscMod.ID, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class ThrowingDiscClient {
    @net.minecraftforge.eventbus.api.listener.SubscribeEvent
    public static void register(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(dev.disc.ModEntities.DISC, ThrowingDiscEntityRenderer::new);
    }
}
