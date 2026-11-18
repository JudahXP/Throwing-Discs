package dev.disc;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscPhysicsTest {
    @Test void reflectionPreservesAngleAndGainsSpeed() {
        Vec3 input = new Vec3(0.5, -1, 1);
        Vec3 result = DiscPhysics.reflect(input, new Vec3(0, 1, 0));
        assertEquals(0.5875, result.x, 1e-10);
        assertEquals(1.175, result.y, 1e-10);
        assertEquals(1.175, result.z, 1e-10);
        assertEquals(input.length() * 1.175, result.length(), 1e-10);
    }
    @Test void repeatedRicochetsStayWithinSafeSpeedLimit() {
        Vec3 velocity=new Vec3(1.25,0,0);
        for(int i=0;i<20;i++) {
            double oldSpeed=velocity.length();
            velocity=DiscPhysics.reflect(velocity,new Vec3(1,0,0));
            assertTrue(velocity.length()>=oldSpeed-1e-10);
            assertTrue(velocity.length()<=3.0+1e-10);
        }
        assertEquals(3.0,velocity.length(),1e-10);
    }
    @Test void catchesWhenFastDiscCrossesPlayerBetweenTicks() {
        assertTrue(DiscPhysics.segmentWithin(new Vec3(-3,0,0), new Vec3(3,0,0), Vec3.ZERO, 0.8));
        assertFalse(DiscPhysics.segmentWithin(new Vec3(-3,2,0), new Vec3(3,2,0), Vec3.ZERO, 0.8));
        assertFalse(DiscPhysics.segmentWithin(new Vec3(2,0,0), new Vec3(3,0,0), Vec3.ZERO, 0.8));
    }
    @Test void zeroLengthSegmentIsSafe() {
        assertTrue(DiscPhysics.segmentWithin(Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, 0.8));
        assertFalse(DiscPhysics.segmentWithin(Vec3.ZERO, Vec3.ZERO, new Vec3(2,0,0), 0.8));
    }
    @Test void returningDiscTurnsAroundAndReachesStationaryThrower() {
        Vec3 position = new Vec3(16, 0, 0), velocity = new Vec3(1.25, 0, 0);
        boolean caught = false;
        for (int tick = 0; tick < 150; tick++) {
            velocity = DiscPhysics.home(velocity, position.scale(-1));
            assertTrue(Double.isFinite(velocity.length()));
            assertTrue(velocity.length() <= 2.25 + 1e-10);
            Vec3 next = position.add(velocity);
            if (DiscPhysics.segmentWithin(position, next, Vec3.ZERO, 0.8)) { caught = true; break; }
            position = next;
        }
        assertTrue(caught, "Homing must reverse even when exactly collinear and initially flying away");
    }
    @Test void returnCanStartAtRest() {
        assertTrue(DiscPhysics.home(Vec3.ZERO, new Vec3(1, 0, 0)).x > 0);
    }

    @Test void enchantmentBonusesAreBounded() {
        assertEquals(12, DiscPhysics.RANGE);
        assertEquals(3, DiscPhysics.rangeBonus(1));
        assertEquals(22, DiscPhysics.rangeBonus(5));
        assertEquals(22, DiscPhysics.rangeBonus(100));
        assertEquals(5, DiscPhysics.speedDamage(5,1.25));
        assertEquals(7, DiscPhysics.speedDamage(5,100), 1e-5);
        var result=DiscPhysics.reflect(new Vec3(1.25,0,0), new Vec3(1,0,0));
        assertEquals(1.25*1.175,result.length(),1e-10);
    }
    @Test void homingCurvesGentlyWithoutAddingSpeed() {
        Vec3 velocity=new Vec3(0,0,1.25), target=new Vec3(4,0,8);
        double previous=0;
        for(int level=1;level<=4;level++) {
            var next=DiscPhysics.steer(velocity,target,level);
            assertEquals(1.25,next.length(),1e-10);
            assertTrue(next.x>previous && next.x<0.1);
            previous=next.x;
        }
        assertEquals(velocity,DiscPhysics.steer(velocity,target,0));
    }
}
