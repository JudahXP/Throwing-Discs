package dev.disc.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.disc.ThrowingDiscEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

public final class ThrowingDiscEntityRenderer extends EntityRenderer<ThrowingDiscEntity, ThrowingDiscEntityRenderer.DiscRenderState> {
    public static final class DiscRenderState extends ThrownItemRenderState { public float yaw, pitch, bank; }
    private final ItemModelResolver resolver;
    public ThrowingDiscEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        resolver = context.getItemModelResolver();
        shadowRadius = 0.3F;
        shadowStrength = 0.65F;
    }
    @Override public DiscRenderState createRenderState() { return new DiscRenderState(); }
    @Override public void extractRenderState(ThrowingDiscEntity entity, DiscRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        var motion = entity.getDeltaMovement();
        state.bank = entity.visualBank();
        state.yaw = (float)Math.toDegrees(Math.atan2(motion.x, motion.z));
        state.pitch = (float)Math.toDegrees(Math.atan2(motion.y, Math.sqrt(motion.x*motion.x + motion.z*motion.z)));
        resolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.NONE, entity);
    }
    @Override public void submit(DiscRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(state.yaw));
        poses.mulPose(Axis.XP.rotationDegrees(-state.pitch));
        poses.mulPose(Axis.ZP.rotationDegrees(state.bank));
        poses.mulPose(Axis.YP.rotationDegrees((state.ageInTicks * 36) % 360));
        poses.mulPose(Axis.XP.rotationDegrees(90));
        poses.scale(0.75F, 0.75F, 0.75F);
        state.item.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poses.popPose();
        super.submit(state, poses, collector, camera);
    }
}
