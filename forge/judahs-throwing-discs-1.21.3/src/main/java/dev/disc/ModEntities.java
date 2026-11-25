package dev.disc;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final net.minecraft.server.level.TicketType<net.minecraft.world.level.ChunkPos> FLIGHT_TICKET = net.minecraft.server.level.TicketType.create("disc_flight",java.util.Comparator.comparingLong(net.minecraft.world.level.ChunkPos::toLong),20);
    public static EntityType<ThrowingDiscEntity> DISC;
    private static void registerEntity(net.minecraftforge.registries.RegisterEvent event) { DISC = register(event,
        Registries.ENTITY_TYPE, ThrowingDiscMod.id("throwing_disc"),
        EntityType.Builder.<ThrowingDiscEntity>of(ThrowingDiscEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.125F).clientTrackingRange(8).updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, ThrowingDiscMod.id("throwing_disc"))));
    }
    public static void initialize(net.minecraftforge.registries.RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.ENTITY_TYPE)) registerEntity(event);
    }
    private static <T, V extends T> V register(net.minecraftforge.registries.RegisterEvent event, ResourceKey<? extends Registry<T>> key, net.minecraft.resources.ResourceLocation id, V value) { event.register(key, id, () -> value); return value; }
    private ModEntities() {}
}
