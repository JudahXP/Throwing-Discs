package dev.disc.client;
@net.neoforged.fml.common.Mod.EventBusSubscriber(modid = dev.disc.ThrowingDiscMod.ID, value = net.neoforged.api.distmarker.Dist.CLIENT, bus = net.neoforged.fml.common.Mod.EventBusSubscriber.Bus.MOD)
public final class ThrowingDiscClient {
    @net.neoforged.bus.api.SubscribeEvent
    public static void register(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(dev.disc.ModEntities.DISC, ThrowingDiscEntityRenderer::new);
    }
}
