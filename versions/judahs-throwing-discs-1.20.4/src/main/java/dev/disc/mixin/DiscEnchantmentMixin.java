package dev.disc.mixin;
import dev.disc.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Enchantment.class) public abstract class DiscEnchantmentMixin {
 @Inject(method="canEnchant",at=@At("HEAD"),cancellable=true) private void disc$allow(ItemStack stack,CallbackInfoReturnable<Boolean> ci){if(stack.getItem() instanceof ThrowingDiscItem)ci.setReturnValue(DiscEnchantments.allowed((Enchantment)(Object)this));}
}
