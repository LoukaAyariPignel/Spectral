package fr.skylined.spectral.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.skylined.spectral.beam.BeamSegment;
import fr.skylined.spectral.block.custom.LightEmitterBlock;
import fr.skylined.spectral.block.entity.LightEmitterBlockEntity;
import fr.skylined.spectral.client.color.WavelengthTintSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LightEmitterBlockEntityRenderer
        implements BlockEntityRenderer<LightEmitterBlockEntity, LightEmitterBlockEntityRenderState> {

    public LightEmitterBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public LightEmitterBlockEntityRenderState createRenderState() {
        return new LightEmitterBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(LightEmitterBlockEntity be, LightEmitterBlockEntityRenderState state,
            float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        state.segments   = be.getCurrentSegments();
        state.facing     = be.getBlockState().getValue(LightEmitterBlock.FACING);
        state.gameTime   = be.getLevel() != null ? be.getLevel().getGameTime() : 0L;
        state.partialTick = partialTicks;
    }

    @Override
    public void submit(LightEmitterBlockEntityRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.segments.isEmpty()) return;

        float animTime = (state.gameTime + state.partialTick) * 0.005f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        applyFacingRotation(poseStack, state.facing);
        poseStack.translate(0, 0.5, 0); // advance to emitter face

        for (BeamSegment seg : state.segments) {
            float len = seg.end() - seg.start();
            if (len <= 0f) continue;

            int color = seg.wavelength() > 0f
                    ? WavelengthTintSource.colorFromWavelength(seg.wavelength())
                    : 0xFFFFFFFF;
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;

            poseStack.pushPose();
            poseStack.translate(0, seg.start(), 0);
            BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION,
                    animTime, len, r, g, b, 0.08f, 0.12f);
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static void applyFacingRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case EAST  -> poseStack.mulPose(Axis.ZP.rotationDegrees(90));
            case WEST  -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
            default    -> {}
        }
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(LightEmitterBlockEntity be) {
        BlockPos pos = be.getBlockPos();
        Direction facing = be.getBlockState().getValue(LightEmitterBlock.FACING);
        return new AABB(pos).expandTowards(
                facing.getStepX() * 32.0,
                facing.getStepY() * 32.0,
                facing.getStepZ() * 32.0
        );
    }
}
