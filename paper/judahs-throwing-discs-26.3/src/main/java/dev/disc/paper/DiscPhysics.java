package dev.disc.paper;
import org.bukkit.util.Vector;
/** Pure vector operations; never mutate arguments owned by a flight. */
public final class DiscPhysics {
 public static final double SPEED=1.25;
 public static Vector reflect(Vector velocity,Vector normal) {
  Vector result=velocity.clone().subtract(normal.clone().multiply(2*velocity.dot(normal)));
  return result.lengthSquared()<1e-10?result:result.normalize().multiply(Math.min(3,velocity.length()*1.175));
 }
 public static Vector home(Vector velocity,Vector displacement) {
  if(displacement.lengthSquared()<1e-10)return velocity.clone().multiply(.75);
  double speed=Math.clamp(displacement.length()*.4,.35,2.25);
  Vector change=displacement.clone().normalize().multiply(speed).subtract(velocity).multiply(.25);
  if(change.lengthSquared()>.09)change.normalize().multiply(.3);
  return velocity.clone().add(change);
 }
 public static boolean mayPhase(boolean returning,Vector start,Vector end,Vector destination) {
  return returning&&start.distanceSquared(destination)<=64&&end.distanceSquared(destination)<=64;
 }
 public static double rangeBonus(int level) { return new double[]{0,3,7,12,17,22}[Math.clamp(level,0,5)]; }
 private DiscPhysics() {}
}
