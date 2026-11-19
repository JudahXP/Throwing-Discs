package dev.disc.mixin;
import dev.disc.*;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AnvilMenu.class) public abstract class DiscAnvilMixin {
 @Inject(method="createResult",at=@At("RETURN")) private void disc$restrict(CallbackInfo ci){
  var menu=(AnvilMenu)(Object)this;var disc=menu.getSlot(0).getItem();var book=menu.getSlot(1).getItem();if(!(disc.getItem() instanceof ThrowingDiscItem))return;
  var applied=EnchantmentHelper.getEnchantments(menu.getSlot(2).getItem()).keySet();
  for(var a:applied)for(var b:applied)if(a!=b&&!a.isCompatibleWith(b)){menu.getSlot(2).set(ItemStack.EMPTY);return;}
  if(book.is(Items.ENCHANTED_BOOK))for(var e:EnchantmentHelper.getEnchantments(book).keySet())if(!DiscEnchantments.allowed(e)){menu.getSlot(2).set(ItemStack.EMPTY);return;}
 }
}
