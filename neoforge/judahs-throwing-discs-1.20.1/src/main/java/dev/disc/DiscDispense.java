package dev.disc;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.BlockSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
public final class DiscDispense {
 public static void initialize() {
  for (DiscTier tier : DiscTier.values()) DispenserBlock.registerBehavior(ModItems.get(tier), new DefaultDispenseItemBehavior() {
   @Override protected ItemStack execute(BlockSource source, ItemStack stack) {
    int slot=-1;
    for(int i=0;i<source.<net.minecraft.world.level.block.entity.DispenserBlockEntity>getEntity().getContainerSize();i++) if(source.<net.minecraft.world.level.block.entity.DispenserBlockEntity>getEntity().getItem(i)==stack) {slot=i;break;}
    var direction=source.getBlockState().getValue(DispenserBlock.FACING);
    var disc=new ThrowingDiscEntity(ModEntities.DISC,source.getLevel());
    disc.launchFromDispenser(source.getLevel(),source.getPos(),new Vec3(direction.getStepX(),direction.getStepY(),direction.getStepZ()),stack.copyWithCount(1),slot);
    if(!source.getLevel().addFreshEntity(disc)) return stack;
    stack.shrink(1);
    if(disc.getItem().hurt(1,source.getLevel().random,null))disc.getItem().shrink(1);
    if(disc.getItem().isEmpty()) disc.discard(); else disc.keepFlightLoaded(source.getLevel());
    return stack;
   }
  });
 }
 private DiscDispense() {}
}
