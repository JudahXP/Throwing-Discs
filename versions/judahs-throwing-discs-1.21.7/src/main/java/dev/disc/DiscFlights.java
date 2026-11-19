package dev.disc;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;

/** Server-thread-only references; removed on catch, unload, and shutdown. */
public final class DiscFlights {
    private static final Set<ThrowingDiscEntity> ACTIVE = new HashSet<>();
    public static void track(ThrowingDiscEntity disc) { ACTIVE.add(disc); }
    public static void forget(ThrowingDiscEntity disc) { ACTIVE.remove(disc); }
    public static void recover(ServerPlayer player) {
        for (ThrowingDiscEntity disc : Set.copyOf(ACTIVE)) {
            if (!disc.isRemoved() && player.getUUID().equals(disc.throwerId())) disc.recoverFor(player);
        }
    }
    public static void clear() { ACTIVE.clear(); }
    private DiscFlights() {}
}
