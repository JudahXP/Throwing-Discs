package dev.disc.paper;
import java.util.Set;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
/** Harvest only when the real custom enchantment is registered and present. */
final class DiscHarvest {
 private static final Set<Material> CROPS=Set.of(Material.WHEAT,Material.CARROTS,Material.POTATOES,Material.BEETROOTS,Material.NETHER_WART,Material.COCOA,Material.TORCHFLOWER_CROP,Material.PITCHER_CROP);
 static void harvest(ItemStack disc,Entity projectile,Player player,Location start,Location end) {
  var enchantment=Registry.ENCHANTMENT.get(new NamespacedKey("throwing_disc","harvest"));
  if(enchantment==null||disc.getEnchantmentLevel(enchantment)==0)return;
  if(player!=null&&(player.getGameMode()==GameMode.ADVENTURE||player.getGameMode()==GameMode.SPECTATOR))return;
  World world=start.getWorld();Vector delta=end.toVector().subtract(start.toVector());double length=delta.length();
  for(int x=(int)Math.floor(Math.min(start.getX(),end.getX())-.15);x<=Math.floor(Math.max(start.getX(),end.getX())+.15);x++)
   for(int y=(int)Math.floor(Math.min(start.getY(),end.getY())-.15);y<=Math.floor(Math.max(start.getY(),end.getY())+.15);y++)
    for(int z=(int)Math.floor(Math.min(start.getZ(),end.getZ())-.15);z<=Math.floor(Math.max(start.getZ(),end.getZ())+.15);z++) {
     if(y<world.getMinHeight()||y>=world.getMaxHeight()||!world.isChunkLoaded(x>>4,z>>4))continue;
     BoundingBox box=new BoundingBox(x-.15,y-.15,z-.15,x+1.15,y+1.15,z+1.15);
     if(!box.contains(start.toVector())&&(length<1e-10||box.rayTrace(start.toVector(),delta,length)==null))continue;
     Block block=world.getBlockAt(x,y,z);Material type=block.getType();var state=block.getBlockData();
     boolean ripe=CROPS.contains(type)&&state instanceof Ageable age&&age.getAge()==age.getMaximumAge();
     ripe|=type==Material.MELON||type==Material.PUMPKIN;
     ripe|=(type==Material.SUGAR_CANE||type==Material.BAMBOO)&&block.getRelative(0,-1,0).getType()==type;
     if(!ripe)continue;
     boolean drops=true;
     if(player!=null){BlockBreakEvent event=new BlockBreakEvent(block,player);if(!event.callEvent())continue;drops=event.isDropItems();}
     else {EntityChangeBlockEvent event=new EntityChangeBlockEvent(projectile,block,Material.AIR.createBlockData());if(!event.callEvent())continue;}
     if(!block.getBlockData().equals(state))continue;
     var loot=block.getDrops(new ItemStack(Material.AIR));block.setType(Material.AIR,true);
     if(drops)for(ItemStack item:loot)world.dropItemNaturally(block.getLocation().add(.5,.5,.5),item);
    }
 }
 private DiscHarvest(){}
}
