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
        state.segments    = be.getCurrentSegments();
        state.facing      = be.getBlockState().getValue(LightEmitterBlock.FACING);
        state.gameTime    = be.getLevel() != null ? be.getLevel().getGameTime() : 0L;
        state.partialTick = partialTicks;
    }

    @Override
    public void submit(LightEmitterBlockEntityRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.segments.isEmpty()) return;

        float animTime = (float) Math.floorMod(state.gameTime, 40) + state.partialTick;

        poseStack.pushPose();

        // submitBeaconBeam does an internal translate(0.5, 0, 0.5) in the rotated
        // pose space. After each rotation the (0.5,0,0.5) maps to different world
        // offsets. We pre-translate to counter the error for SOUTH and EAST.
        // NORTH: R×(0.5,0,0.5) = (0.5, 0.5, 0) → correct N-face center, no fix needed
        // SOUTH: R×(0.5,0,0.5) = (0.5,-0.5, 0) → needs +1Y +1Z to reach S-face center
        // EAST:  R×(0.5,0,0.5) = (0,  -0.5,0.5) → needs +1X +1Y to reach E-face center
        // WEST:  R×(0.5,0,0.5) = (0,   0.5,0.5) → correct W-face center, no fix needed
        switch (state.facing) {
            case SOUTH -> poseStack.translate(0, 1, 1);
            case EAST  -> poseStack.translate(1, 1, 0);
            default    -> {}
        }

        applyFacingRotation(poseStack, state.facing);

        for (BeamSegment seg : state.segments) {
            int segStart  = (int) seg.start();
            int segHeight = (int) seg.end() - segStart;
            if (segHeight <= 0) continue;

            int colorARGB = seg.wavelength() > 0f
                    ? WavelengthTintSource.colorFromWavelength(seg.wavelength())
                    : 0xFFFFFFFF;

            BeaconRenderer.submitBeaconBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION,
                    1.0f, animTime, segStart, segHeight, colorARGB, 0.15f, 0.20f);
        }

        poseStack.popPose();
    }

    private static void applyFacingRotation(PoseStack poseStack, Direction facing) {
        // Right-hand rule — makes pose-Y point in FACING direction:
        // XP -90°: Y+ → -Z (North)    XP +90°: Y+ → +Z (South)
        // ZP -90°: Y+ → +X (East)     ZP +90°: Y+ → -X (West)
        switch (facing) {
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case EAST  -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90));
            case WEST  -> poseStack.mulPose(Axis.ZP.rotationDegrees(90));
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
