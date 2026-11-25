package dev.disc;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
public final class DiscHarvest {
 public static void harvest(ServerLevel level, ThrowingDiscEntity disc, Vec3 start, Vec3 end) {
  if(disc.enchantmentLevel("harvest")==0) return;
  var bounds=new AABB(start,end).inflate(0.15);
  for(BlockPos cursor:BlockPos.betweenClosed(BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ),BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ))) {
   BlockPos pos=cursor.immutable();
   if(!level.hasChunkAt(pos)) continue;
   var box=new AABB(pos).inflate(0.15);
   if(!box.contains(start) && box.clip(start,end).isEmpty()) continue;
   if(disc.getOwner() instanceof ServerPlayer player && (!player.mayBuild() || !level.mayInteract(player,pos))) continue;
   var state=level.getBlockState(pos);var block=state.getBlock();
   boolean ripe=block instanceof CropBlock crop && crop.isMaxAge(state);
   ripe |= state.is(Blocks.NETHER_WART) && state.getValue(NetherWartBlock.AGE)==3;
   ripe |= state.is(Blocks.COCOA) && state.getValue(CocoaBlock.AGE)==2;
   ripe |= state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN);
   ripe |= state.is(Blocks.SUGAR_CANE) && level.getBlockState(pos.below()).is(Blocks.SUGAR_CANE);
   ripe |= state.is(Blocks.BAMBOO) && level.getBlockState(pos.below()).is(Blocks.BAMBOO);
   if(ripe) level.destroyBlock(pos,true,disc.getOwner());
  }
 }
 private DiscHarvest() {}
}
