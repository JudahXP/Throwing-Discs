package dev.disc;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
@net.neoforged.fml.common.Mod(ThrowingDiscMod.ID)
public final class ThrowingDiscMod {
    public static final String ID = "throwing_disc";
    public static ResourceLocation id(String path) { return new ResourceLocation(ID, path); }
    public ThrowingDiscMod(net.neoforged.bus.api.IEventBus bus) {
        bus.addListener(ThrowingDiscMod::register);
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e) -> e.enqueueWork(DiscDispense::initialize));
        bus.addListener(ThrowingDiscMod::creative);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(ThrowingDiscMod::logout);
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppingEvent e) -> e.getServer().getPlayerList().getPlayers().forEach(DiscFlights::recover));
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e) -> DiscFlights.clear());
    }
    private static void register(net.neoforged.neoforge.registries.RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) ModItems.initialize(event);
        ModEntities.initialize(event);
        if (event.getRegistryKey().equals(Registries.ENCHANTMENT)) DiscEnchantments.initialize(event);
    }
    private static void creative(net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(net.minecraft.world.item.CreativeModeTabs.COMBAT))
            for (DiscTier tier : new DiscTier[] { DiscTier.COPPER, DiscTier.IRON, DiscTier.GOLD, DiscTier.DIAMOND, DiscTier.NETHERITE }) event.accept(ModItems.get(tier));
    }
    private static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) DiscFlights.recover(player);
    }
}
