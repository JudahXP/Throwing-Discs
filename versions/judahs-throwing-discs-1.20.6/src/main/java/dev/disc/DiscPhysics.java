package dev.disc;

import net.minecraft.world.phys.Vec3;

public final class DiscPhysics {
    public static final double SPEED = 1.25;
    public static final double RANGE = 12;
    public static final double BOUNCE_BONUS = 12;
    public static final int MAX_OUTBOUND_TICKS = 160;
    public static final int MAX_FLIGHT_TICKS = 320;
    public static final double RICOCHET_ACCELERATION = 1.175;
    public static final double MAX_RICOCHET_SPEED = 3.0;
    public static Vec3 reflect(Vec3 velocity, Vec3 unitNormal) {
        Vec3 reflected = velocity.subtract(unitNormal.scale(2 * velocity.dot(unitNormal)));
        return reflected.normalize().scale(Math.min(MAX_RICOCHET_SPEED,
            reflected.length() * RICOCHET_ACCELERATION));
    }
    public static Vec3 home(Vec3 velocity, Vec3 displacement) {
        if (displacement.lengthSqr() < 1.0e-10) return velocity.scale(0.75);
        double speed = net.minecraft.util.Mth.clamp(displacement.length() * 0.4, 0.35, 2.25);
        Vec3 desired = displacement.normalize().scale(speed);
        // Ease toward the owner directly; never introduce a sideways U-turn arc.
        Vec3 change = desired.subtract(velocity).scale(0.25);
        if (change.lengthSqr() > 0.09) change = change.normalize().scale(0.3);
        return velocity.add(change);
    }
    public static boolean segmentWithin(Vec3 a, Vec3 b, Vec3 point, double radius) {
        Vec3 delta = b.subtract(a);
        double fraction = delta.lengthSqr() < 1.0e-10 ? 0
            : net.minecraft.util.Mth.clamp(point.subtract(a).dot(delta) / delta.lengthSqr(), 0, 1);
        return a.add(delta.scale(fraction)).distanceToSqr(point) <= radius * radius;
    }
    public static double rangeBonus(int level) {
        return new double[]{0,3,7,12,17,22}[net.minecraft.util.Mth.clamp(level,0,5)];
    }
    public static float speedDamage(float base, double speed) {
        return base * (float)(1 + net.minecraft.util.Mth.clamp((speed - SPEED) * 0.25, 0, 0.4));
    }
    public static Vec3 steer(Vec3 velocity, Vec3 displacement, int level) {
        if (level <= 0 || velocity.lengthSqr() < 1.0e-10 || displacement.lengthSqr() < 1.0e-10) return velocity;
        double blend = 0.025 * net.minecraft.util.Mth.clamp(level, 0, 4);
        return velocity.normalize().scale(1-blend).add(displacement.normalize().scale(blend)).normalize().scale(velocity.length());
    }
    private DiscPhysics() {}
}
