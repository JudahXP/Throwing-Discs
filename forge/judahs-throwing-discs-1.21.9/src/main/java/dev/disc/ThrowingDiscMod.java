package dev.disc;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
@net.minecraftforge.fml.common.Mod(ThrowingDiscMod.ID)
public final class ThrowingDiscMod {
    public static final String ID = "throwing_disc";
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ID, path); }
    public ThrowingDiscMod(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext context) {
        var bus = context.getModBusGroup();
        net.minecraftforge.registries.RegisterEvent.getBus(bus).addListener(ThrowingDiscMod::register);
        net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent.getBus(bus).addListener(e -> e.enqueueWork(DiscDispense::initialize));
        net.minecraftforge.event.BuildCreativeModeTabContentsEvent.BUS.addListener(ThrowingDiscMod::creative);
        net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent.BUS.addListener(ThrowingDiscMod::logout);
        net.minecraftforge.event.server.ServerStoppingEvent.BUS.addListener(e -> e.getServer().getPlayerList().getPlayers().forEach(DiscFlights::recover));
        net.minecraftforge.event.server.ServerStoppedEvent.BUS.addListener(e -> DiscFlights.clear());
    }
    private static void register(net.minecraftforge.registries.RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) ModItems.initialize(event);
        ModEntities.initialize(event);
    }
    private static void creative(net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(net.minecraft.world.item.CreativeModeTabs.COMBAT))
            for (DiscTier tier : DiscTier.values()) event.accept(ModItems.get(tier));
    }
    private static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) DiscFlights.recover(player);
    }
}
