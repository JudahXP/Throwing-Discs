package dev.disc.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.disc.ThrowingDiscEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
public final class ThrowingDiscEntityRenderer extends EntityRenderer<ThrowingDiscEntity> {
 private final ItemRenderer items;
 public ThrowingDiscEntityRenderer(EntityRendererProvider.Context context){super(context);items=context.getItemRenderer();shadowRadius=.3F;shadowStrength=.65F;}
 @Override public ResourceLocation getTextureLocation(ThrowingDiscEntity e){return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;}
 @Override public void render(ThrowingDiscEntity e,float yaw,float partial,PoseStack poses,MultiBufferSource buffers,int light){
  var motion=e.getDeltaMovement();poses.pushPose();poses.mulPose(Axis.YP.rotationDegrees((float)Math.toDegrees(Math.atan2(motion.x,motion.z))));poses.mulPose(Axis.XP.rotationDegrees((float)-Math.toDegrees(Math.atan2(motion.y,Math.sqrt(motion.x*motion.x+motion.z*motion.z)))));poses.mulPose(Axis.ZP.rotationDegrees(e.visualBank()));poses.mulPose(Axis.YP.rotationDegrees(((e.tickCount+partial)*36)%360));poses.mulPose(Axis.XP.rotationDegrees(90));poses.scale(.75F,.75F,.75F);
  items.renderStatic(e.getItem(),ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,poses,buffers,e.level(),e.getId());poses.popPose();super.render(e,yaw,partial,poses,buffers,light);
 }
}
