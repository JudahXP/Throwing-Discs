package dev.disc.client;

import dev.disc.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public final class ThrowingDiscClient implements ClientModInitializer {
    @Override public void onInitializeClient() {
        EntityRenderers.register(ModEntities.DISC, ThrowingDiscEntityRenderer::new);
    }
}
