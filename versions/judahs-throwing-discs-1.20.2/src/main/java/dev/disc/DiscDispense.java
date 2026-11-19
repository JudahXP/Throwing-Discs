package dev.disc;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
public final class DiscDispense {
 public static void initialize() {
  for (DiscTier tier : DiscTier.values()) DispenserBlock.registerBehavior(ModItems.get(tier), new DefaultDispenseItemBehavior() {
   @Override protected ItemStack execute(BlockSource source, ItemStack stack) {
    int slot=-1;
    for(int i=0;i<source.blockEntity().getContainerSize();i++) if(source.blockEntity().getItem(i)==stack) {slot=i;break;}
    var direction=source.state().getValue(DispenserBlock.FACING);
    var disc=new ThrowingDiscEntity(ModEntities.DISC,source.level());
    disc.launchFromDispenser(source.level(),source.pos(),new Vec3(direction.getStepX(),direction.getStepY(),direction.getStepZ()),stack.copyWithCount(1),slot);
    if(!source.level().addFreshEntity(disc)) return stack;
    stack.shrink(1);
    if(disc.getItem().hurt(1,source.level().random,null))disc.getItem().shrink(1);
    if(disc.getItem().isEmpty()) disc.discard(); else disc.keepFlightLoaded(source.level());
    return stack;
   }
  });
 }
 private DiscDispense() {}
}
