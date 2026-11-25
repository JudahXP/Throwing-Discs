package dev.disc.client;
@net.neoforged.fml.common.EventBusSubscriber(modid = dev.disc.ThrowingDiscMod.ID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public final class ThrowingDiscClient {
    @net.neoforged.bus.api.SubscribeEvent
    public static void register(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(dev.disc.ModEntities.DISC, ThrowingDiscEntityRenderer::new);
    }
}
