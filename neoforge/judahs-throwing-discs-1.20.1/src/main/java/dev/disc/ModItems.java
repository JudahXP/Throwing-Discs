package dev.disc;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class ModItems {
    private static final Map<DiscTier, ThrowingDiscItem> ITEMS = new EnumMap<>(DiscTier.class);
    public static ThrowingDiscItem get(DiscTier tier) { return ITEMS.get(tier); }
    public static void initialize(net.minecraftforge.registries.RegisterEvent event) {
        for (DiscTier tier : DiscTier.values()) {
            var id = ThrowingDiscMod.id(tier.id + "_throwing_disc");
            var key = ResourceKey.create(Registries.ITEM, id);
            var properties = new Item.Properties().durability(tier.material.getUses() / 2);
            if (tier == DiscTier.COPPER) properties.durability(95);
            if (tier == DiscTier.NETHERITE) properties.fireResistant();
            var item = new ThrowingDiscItem(properties, tier);
            event.register(Registries.ITEM, id, () -> item);
            ITEMS.put(tier, item);
        }
    }
    private ModItems() {}
}
