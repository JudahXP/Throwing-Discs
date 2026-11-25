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
    public static void initialize(net.neoforged.neoforge.registries.RegisterEvent event) {
        for (DiscTier tier : DiscTier.values()) {
            var id = ThrowingDiscMod.id(tier.id + "_throwing_disc");
            var key = ResourceKey.create(Registries.ITEM, id);
            var properties = new Item.Properties().setId(key).durability(tier.material.durability() / 2)
                .enchantable(tier.material.enchantmentValue()).repairable(tier.material.repairItems());
            properties.attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,
                        tier.melee - 1.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_SPEED_ID,
                        -3.0, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),
                    net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).build());
            if (tier == DiscTier.COPPER) properties.durability(95).enchantable(13).repairable(net.minecraft.world.item.Items.COPPER_INGOT);
            if (tier == DiscTier.NETHERITE) properties.fireResistant();
            var item = new ThrowingDiscItem(properties, tier);
            event.register(Registries.ITEM, id, () -> item);
            ITEMS.put(tier, item);
        }
    }
    private ModItems() {}
}
