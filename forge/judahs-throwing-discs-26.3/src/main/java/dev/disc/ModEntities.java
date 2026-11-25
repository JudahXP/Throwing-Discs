package dev.disc;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static net.minecraft.server.level.TicketType FLIGHT_TICKET;
    private static void registerTicket(net.minecraftforge.registries.RegisterEvent event) { FLIGHT_TICKET = register(event,
        Registries.TICKET_TYPE, ThrowingDiscMod.id("disc_flight"),
        new net.minecraft.server.level.TicketType(20, net.minecraft.server.level.TicketType.FLAG_LOADING
            | net.minecraft.server.level.TicketType.FLAG_SIMULATION
            | net.minecraft.server.level.TicketType.FLAG_KEEP_DIMENSION_ACTIVE));
    }
    public static EntityType<ThrowingDiscEntity> DISC;
    private static void registerEntity(net.minecraftforge.registries.RegisterEvent event) { DISC = register(event,
        Registries.ENTITY_TYPE, ThrowingDiscMod.id("throwing_disc"),
        EntityType.Builder.<ThrowingDiscEntity>of(ThrowingDiscEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.125F).clientTrackingRange(8).updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ThrowingDiscMod.id("throwing_disc"))));
    }
    public static void initialize(net.minecraftforge.registries.RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ENTITY_TYPE)) registerEntity(event);
        if (event.getRegistryKey().equals(Registries.TICKET_TYPE)) registerTicket(event);
    }
    private static <T, V extends T> V register(net.minecraftforge.registries.RegisterEvent event, ResourceKey<? extends Registry<T>> key, net.minecraft.resources.Identifier id, V value) { event.register(key, id, () -> value); return value; }
    private ModEntities() {}
}
