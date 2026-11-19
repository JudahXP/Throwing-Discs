package dev.disc;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final net.minecraft.server.level.TicketType FLIGHT_TICKET = Registry.register(
        BuiltInRegistries.TICKET_TYPE, ThrowingDiscMod.id("disc_flight"),
        new net.minecraft.server.level.TicketType(20, net.minecraft.server.level.TicketType.FLAG_LOADING
            | net.minecraft.server.level.TicketType.FLAG_SIMULATION
            | net.minecraft.server.level.TicketType.FLAG_KEEP_DIMENSION_ACTIVE));
    public static final EntityType<ThrowingDiscEntity> DISC = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, ThrowingDiscMod.id("throwing_disc"),
        EntityType.Builder.<ThrowingDiscEntity>of(ThrowingDiscEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.125F).clientTrackingRange(8).updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ThrowingDiscMod.id("throwing_disc"))));
    public static void initialize() {}
    private ModEntities() {}
}
