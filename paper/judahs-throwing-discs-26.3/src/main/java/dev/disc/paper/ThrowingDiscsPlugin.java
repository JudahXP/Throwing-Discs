package dev.disc.paper;

import java.util.*;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Dispenser;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.util.RayTraceResult;

public final class ThrowingDiscsPlugin extends JavaPlugin implements Listener {
 private final List<Flight> flights=new ArrayList<>();
 private NamespacedKey tierKey;
 @Override public void onEnable() {
  tierKey=new NamespacedKey(this,"tier");getServer().getPluginManager().registerEvents(this,this);
  for(DiscTier tier:DiscTier.values()) {
   if(tier==DiscTier.NETHERITE)continue; // Smithing integration is a separate feature.
   ShapedRecipe recipe=new ShapedRecipe(new NamespacedKey(this,tier.id+"_disc"),item(tier));
   recipe.shape(" M ","M M"," M ");recipe.setIngredient('M',Material.valueOf(tier==DiscTier.DIAMOND?"DIAMOND":(tier==DiscTier.GOLD?"GOLD":tier.name())+"_INGOT"));getServer().addRecipe(recipe);
  }
  getServer().getScheduler().runTaskTimer(this,()->{for(Flight f:List.copyOf(flights))try{f.tick();}catch(RuntimeException e){getLogger().warning("Flight recovered after error: "+e);f.finish(false);}},1,1);
 }
 @Override public void onDisable() { for(Flight f:List.copyOf(flights))f.finish(true);for(DiscTier t:DiscTier.values())getServer().removeRecipe(new NamespacedKey(this,t.id+"_disc")); }
 private DiscTier tier(ItemStack stack) {
  if(stack==null||!stack.hasItemMeta())return null;
  String id=stack.getItemMeta().getPersistentDataContainer().get(tierKey,PersistentDataType.STRING);
  for(DiscTier t:DiscTier.values())if(t.id.equals(id))return t;return null;
 }
 private ItemStack item(DiscTier tier) {
  ItemStack stack=new ItemStack(Material.IRON_NUGGET);ItemMeta meta=stack.getItemMeta();
  meta.displayName(Component.text((tier==DiscTier.GOLD?"Golden":tier.id.substring(0,1).toUpperCase()+tier.id.substring(1))+" Throwing Disc"));
  meta.setMaxStackSize(1);meta.setItemModel(new NamespacedKey("throwing_disc",tier.id+"_throwing_disc"));
  meta.getPersistentDataContainer().set(tierKey,PersistentDataType.STRING,tier.id);
  if(meta instanceof org.bukkit.inventory.meta.Damageable d)d.setMaxDamage(tier.durability);
  stack.setItemMeta(meta);return stack;
 }
 @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
  if(!(sender instanceof Player p)||args.length!=1)return false;
  for(DiscTier t:DiscTier.values())if(t.id.equalsIgnoreCase(args[0])){p.getInventory().addItem(item(t)).values().forEach(s->p.getWorld().dropItemNaturally(p.getLocation(),s));return true;}return false;
 }
 @EventHandler(priority=EventPriority.HIGHEST) public void use(PlayerInteractEvent event) {
  if(!event.getAction().isRightClick()||event.getHand()==null)return;
  DiscTier tier=tier(event.getItem());if(tier==null)return;
  if(event.useItemInHand()==Event.Result.DENY)return;
  Player p=event.getPlayer();if(p.getGameMode()==GameMode.SPECTATOR||p.hasCooldown(Material.IRON_NUGGET))return;
  ItemStack held=p.getInventory().getItem(event.getHand());if(held.getAmount()!=1)return;
  event.setCancelled(true);
  ItemStack carried=held.clone();ItemMeta meta=carried.getItemMeta();
  if(meta instanceof org.bukkit.inventory.meta.Damageable d){d.setDamage(d.getDamage()+1);if(d.getDamage()>=tier.durability){p.getInventory().setItem(event.getHand(),null);p.playSound(p,Sound.ENTITY_ITEM_BREAK,1,1);return;}carried.setItemMeta(meta);}
  Location start=p.getEyeLocation().subtract(0,.15,0);
  Flight f=new Flight(p,tier,carried,start,start.getDirection().multiply(DiscPhysics.SPEED));
  p.getInventory().setItem(event.getHand(),null);flights.add(f);p.setCooldown(Material.IRON_NUGGET,6);
 }
 @EventHandler public void quit(PlayerQuitEvent event) {for(Flight f:List.copyOf(flights))if(event.getPlayer().getUniqueId().equals(f.owner))f.finish(true);}
 @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
 public void dispense(org.bukkit.event.block.BlockDispenseEvent event) {
  DiscTier kind=tier(event.getItem());
  if(kind==null||!(event.getBlock().getState() instanceof Dispenser))return;
  event.setCancelled(true);
  Location origin=event.getBlock().getLocation();ItemStack expected=event.getItem().clone();expected.setAmount(1);
  // Vanilla restores its temporarily removed item after a cancelled event returns.
  getServer().getScheduler().runTask(this,()->{
   if(!origin.isWorldLoaded()||!origin.getWorld().isChunkLoaded(origin.getBlockX()>>4,origin.getBlockZ()>>4))return;
   if(!(origin.getBlock().getState() instanceof Dispenser dispenser))return;
   Inventory inventory=dispenser.getInventory();
   for(int slot=0;slot<inventory.getSize();slot++) {
    ItemStack stored=inventory.getItem(slot);if(stored==null||!stored.isSimilar(expected)||stored.getAmount()!=1)continue;
    ItemStack carried=stored.clone();ItemMeta meta=carried.getItemMeta();
    if(meta instanceof org.bukkit.inventory.meta.Damageable d){d.setDamage(d.getDamage()+1);if(d.getDamage()>=kind.durability){inventory.setItem(slot,null);return;}carried.setItemMeta(meta);}
    Vector direction=((org.bukkit.block.data.Directional)dispenser.getBlockData()).getFacing().getDirection();
    Location start=origin.clone().add(.5,.5,.5).add(direction.clone().multiply(.85));
    Flight flight=new Flight(null,kind,carried,start,direction.multiply(DiscPhysics.SPEED));flight.dispenser=origin.clone();
    inventory.setItem(slot,null);flights.add(flight);return;
   }
  });
 }
 private final class Flight {
  final UUID owner;final DiscTier tier;final ItemStack carried;final World world;final ItemDisplay display;
  final Set<UUID> hit=new HashSet<>();Location dispenser;Location position;Vector velocity;boolean returning,finished;int age,bounces;double distance;
  Flight(Player player,DiscTier tier,ItemStack stack,Location start,Vector velocity) {
   owner=player==null?null:player.getUniqueId();this.tier=tier;carried=stack;world=start.getWorld();position=start.clone();this.velocity=velocity.clone();
   display=world.spawn(start,ItemDisplay.class,d->{d.setItemStack(stack);d.setPersistent(false);d.setTeleportDuration(1);d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.FIXED);});
  }
  void tick() {
   Player player=owner==null?null:getServer().getPlayer(owner);
   if(++age>320){finish(false);return;}
   Location target;
   if(dispenser!=null){
    if(!world.isChunkLoaded(dispenser.getBlockX()>>4,dispenser.getBlockZ()>>4)||!(dispenser.getBlock().getState() instanceof Dispenser)){finish(false);return;}
    target=dispenser.clone().add(.5,.5,.5);
   }else{
    if(player==null||!player.isOnline()||player.isDead()||player.getGameMode()==GameMode.SPECTATOR||player.getWorld()!=world){finish(false);return;}
    target=player.getEyeLocation().subtract(0,.35,0);
   }
   if(!returning&&(age>=160||distance>=tier.range+bounces*12||velocity.lengthSquared()<1e-10)){returning=true;hit.clear();}
   if(returning)velocity=DiscPhysics.home(velocity,target.toVector().subtract(position.toVector()));
   Location end=position.clone().add(velocity);
   if(!world.isChunkLoaded(end.getBlockX()>>4,end.getBlockZ()>>4)){finish(false);return;}
   if(returning&&segmentDistanceSquared(position.toVector(),end.toVector(),target.toVector())<=.64){finish(true);return;}
   boolean phase=DiscPhysics.mayPhase(returning,position.toVector(),end.toVector(),target.toVector());
   RayTraceResult block=phase?null:world.rayTraceBlocks(position,velocity,velocity.length(),FluidCollisionMode.NEVER,true);
   if(block!=null&&block.getHitBlockFace()!=null){
    end=block.getHitPosition().toLocation(world).add(block.getHitBlockFace().getDirection().multiply(.03));
    velocity=DiscPhysics.reflect(velocity,block.getHitBlockFace().getDirection());if(!returning)bounces++;
    world.playSound(end,Sound.ITEM_TRIDENT_HIT_GROUND,.4f,1.4f);
   }
   DiscHarvest.harvest(carried,display,player,position,end);
   Vector segment=end.toVector().subtract(position.toVector());
   if(segment.lengthSquared()>1e-10) {
    RayTraceResult entity;
    while((entity=world.rayTraceEntities(position,segment,segment.length(),.25,e->e instanceof LivingEntity&&!e.getUniqueId().equals(owner)&&!hit.contains(e.getUniqueId())&&(!(e instanceof Player q)||(q.getGameMode()!=GameMode.SPECTATOR&&q.getGameMode()!=GameMode.CREATIVE))))!=null) {
     LivingEntity victim=(LivingEntity)entity.getHitEntity();hit.add(victim.getUniqueId());if(player!=null)victim.damage(tier.thrownDamage,player);else victim.damage(tier.thrownDamage,display);
    }
   }
   distance+=position.distance(end);position=end;position.setYaw(age*40%360);display.teleport(position);
  }
  void finish(boolean recover) {
   if(finished)return;finished=true;display.remove();flights.remove(this);
   Player p=owner==null?null:getServer().getPlayer(owner);
   if(recover&&dispenser!=null&&world.isChunkLoaded(dispenser.getBlockX()>>4,dispenser.getBlockZ()>>4)&&dispenser.getBlock().getState() instanceof Dispenser holder){
    holder.getInventory().addItem(carried).values().forEach(s->world.dropItemNaturally(dispenser.clone().add(.5,.5,.5),s));return;
   }
   if(recover&&p!=null){p.getInventory().addItem(carried).values().forEach(s->p.getWorld().dropItemNaturally(p.getLocation(),s));}
   else world.dropItemNaturally(position,carried);
  }
 }
 private static double segmentDistanceSquared(Vector a,Vector b,Vector p) {
  Vector delta=b.clone().subtract(a);double t=delta.lengthSquared()<1e-10?0:Math.clamp(p.clone().subtract(a).dot(delta)/delta.lengthSquared(),0,1);return a.clone().add(delta.multiply(t)).distanceSquared(p);
 }
}
