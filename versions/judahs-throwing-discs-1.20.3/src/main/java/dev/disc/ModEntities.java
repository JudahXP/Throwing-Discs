package dev.disc;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final net.minecraft.server.level.TicketType<net.minecraft.world.level.ChunkPos> FLIGHT_TICKET = net.minecraft.server.level.TicketType.create("disc_flight",java.util.Comparator.comparingLong(net.minecraft.world.level.ChunkPos::toLong),20);
    public static final EntityType<ThrowingDiscEntity> DISC = Registry.register(
        BuiltInRegistries.ENTITY_TYPE, ThrowingDiscMod.id("throwing_disc"),
        EntityType.Builder.<ThrowingDiscEntity>of(ThrowingDiscEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.125F).clientTrackingRange(8).updateInterval(1)
            .build("throwing_disc:throwing_disc"));
    public static void initialize() {}
    private ModEntities() {}
}
