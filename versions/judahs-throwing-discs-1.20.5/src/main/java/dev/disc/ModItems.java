package dev.disc;

import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class ModItems {
    private static final Map<DiscTier, ThrowingDiscItem> ITEMS = new EnumMap<>(DiscTier.class);
    public static ThrowingDiscItem get(DiscTier tier) { return ITEMS.get(tier); }
    public static void initialize() {
        for (DiscTier tier : DiscTier.values()) {
            var id = ThrowingDiscMod.id(tier.id + "_throwing_disc");
            var key = ResourceKey.create(Registries.ITEM, id);
            var properties = new Item.Properties().durability(tier.material.getUses() / 2);
            properties.attributes(net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_DAMAGE_UUID,"Disc damage",tier.melee-1,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,new net.minecraft.world.entity.ai.attributes.AttributeModifier(Item.BASE_ATTACK_SPEED_UUID,"Disc speed",-3,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).build());
            if (tier == DiscTier.COPPER) properties.durability(95);
            if (tier == DiscTier.NETHERITE) properties.fireResistant();
            ITEMS.put(tier, Registry.register(BuiltInRegistries.ITEM, key,
                new ThrowingDiscItem(properties, tier)));
        }
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
            for (DiscTier tier : DiscTier.values()) entries.accept(get(tier));
        });
    }
    private ModItems() {}
}
