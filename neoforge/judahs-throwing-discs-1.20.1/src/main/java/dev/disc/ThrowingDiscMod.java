package dev.disc;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
@net.minecraftforge.fml.common.Mod(ThrowingDiscMod.ID)
public final class ThrowingDiscMod {
    public static final String ID = "throwing_disc";
    public static ResourceLocation id(String path) { return new ResourceLocation(ID, path); }
    public ThrowingDiscMod() {
        var context = net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get();
        var bus = context.getModEventBus();
        bus.addListener(ThrowingDiscMod::register);
        bus.addListener((net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent e) -> e.enqueueWork(DiscDispense::initialize));
        bus.addListener(ThrowingDiscMod::creative);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(ThrowingDiscMod::logout);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.server.ServerStoppingEvent e) -> e.getServer().getPlayerList().getPlayers().forEach(DiscFlights::recover));
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.server.ServerStoppedEvent e) -> DiscFlights.clear());
    }
    private static void register(net.minecraftforge.registries.RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ITEM)) ModItems.initialize(event);
        ModEntities.initialize(event);
        if (event.getRegistryKey().equals(Registries.ENCHANTMENT)) DiscEnchantments.initialize(event);
    }
    private static void creative(net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(net.minecraft.world.item.CreativeModeTabs.COMBAT))
            for (DiscTier tier : new DiscTier[] { DiscTier.COPPER, DiscTier.IRON, DiscTier.GOLD, DiscTier.DIAMOND, DiscTier.NETHERITE }) event.accept(ModItems.get(tier));
    }
    private static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) DiscFlights.recover(player);
    }
}
