package dev.disc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

public final class ThrowingDiscMod implements ModInitializer {
    public static final String ID = "throwing_disc";
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(ID, path); }
    @Override public void onInitialize() {
        ModItems.initialize();
        ModEntities.initialize();
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
            DiscFlights.recover(handler.player));
        ServerLifecycleEvents.SERVER_STOPPING.register(server ->
            server.getPlayerList().getPlayers().forEach(DiscFlights::recover));
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> DiscFlights.clear());
    }
}
