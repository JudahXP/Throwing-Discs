package dev.disc.mixin;
import dev.disc.*;
import java.util.List;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(EnchantmentHelper.class) public abstract class DiscEnchantingTableMixin {
 @Inject(method="getAvailableEnchantmentResults",at=@At("RETURN")) private static void disc$filter(int power,ItemStack stack,boolean treasure,CallbackInfoReturnable<List<EnchantmentInstance>> ci){ci.getReturnValue().removeIf(e->(stack.getItem() instanceof ThrowingDiscItem&&!DiscEnchantments.allowed(e.enchantment))||(!(stack.getItem() instanceof ThrowingDiscItem)&&!stack.is(Items.BOOK)&&e.enchantment instanceof DiscEnchantments.DiscEnchantment));}
}
