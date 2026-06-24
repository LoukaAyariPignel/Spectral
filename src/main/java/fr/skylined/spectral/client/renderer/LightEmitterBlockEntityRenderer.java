package fr.skylined.spectral.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.skylined.spectral.beam.BeamSegment;
import fr.skylined.spectral.block.entity.LightEmitterBlockEntity;
import fr.skylined.spectral.client.color.WavelengthTintSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
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
        state.dirX        = be.getDirX();
        state.dirY        = be.getDirY();
        state.dirZ        = be.getDirZ();
        state.gameTime    = be.getLevel() != null ? be.getLevel().getGameTime() : 0L;
        state.partialTick = partialTicks;
        state.beamAlpha   = 0.5f + 0.5f * (be.getStoredPhotons() / (float) LightEmitterBlockEntity.MAX_PHOTONS);
    }

    @Override
    public void submit(LightEmitterBlockEntityRenderState state, PoseStack poseStack,
            SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.segments.isEmpty()) return;

        float animTime = (float) Math.floorMod(state.gameTime, 40) + state.partialTick;
        float dx = state.dirX, dy = state.dirY, dz = state.dirZ;

        poseStack.pushPose();

        // DDA t values are measured from the emitter block center, so we translate there.
        // The beam geometry starts at Y ≈ 0.5 (= face of the block), hidden inside for Y < 0.5.
        poseStack.translate(0.5, 0.5, 0.5);
        alignYToDirection(poseStack, dx, dy, dz);

        final int   FADE_BLOCKS  = 5;
        final int   FADE_START   = LightEmitterBlockEntity.BEAM_MAX_RANGE - FADE_BLOCKS;
        final float RADIUS_INNER = 0.15f;
        final float RADIUS_GLOW  = 0.20f;
        final float FADE_MIN     = 0.20f;

        for (BeamSegment seg : state.segments) {
            int segStart = (int) seg.start();
            int segEnd   = (int) seg.end();
            if (segEnd <= segStart) continue;

            int baseAlpha = Math.round(state.beamAlpha * 255f) & 0xFF;
            int rgb = seg.wavelength() > 0f
                    ? (WavelengthTintSource.colorFromWavelength(seg.wavelength()) & 0x00FFFFFF)
                    : 0x00FFFFFF;
            int baseColor = (baseAlpha << 24) | rgb;

            boolean fades  = segEnd >= LightEmitterBlockEntity.BEAM_MAX_RANGE;
            int solidEnd   = fades ? Math.max(segStart, Math.min(segEnd, FADE_START)) : segEnd;

            if (solidEnd > segStart) {
                SpectralBeamRenderer.submitTranslucentBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION,
                        1.0f, animTime, segStart, solidEnd - segStart,
                        baseColor, RADIUS_INNER, RADIUS_GLOW);
            }

            if (fades) {
                int fadeFrom = Math.max(segStart, FADE_START);
                for (int b = fadeFrom; b < segEnd; b++) {
                    float t     = (b - FADE_START + 1) / (float) FADE_BLOCKS;
                    float scale = 1f - t * (1f - FADE_MIN);
                    SpectralBeamRenderer.submitTranslucentBeam(poseStack, collector, BeaconRenderer.BEAM_LOCATION,
                            1.0f, animTime, b, 1,
                            ARGB.multiplyAlpha(baseColor, scale), RADIUS_INNER, RADIUS_GLOW);
                }
            }
        }

        poseStack.popPose();
    }

    /**
     * Shortest-arc quaternion that rotates local Y+ to the given direction.
     * Works for any unit vector: cardinal, 22.5° sign steps, diagonal, or arbitrary pitch.
     */
    private static void alignYToDirection(PoseStack poseStack, float dx, float dy, float dz) {
        if (dy > 0.9999f) return; // already pointing up
        if (dy < -0.9999f) {
            // Antiparallel to Y+ — rotationTo is unstable, use explicit 180° around X
            poseStack.mulPose(Axis.XP.rotationDegrees(180));
            return;
        }
        poseStack.mulPose(new Quaternionf().rotationTo(0f, 1f, 0f, dx, dy, dz));
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
        return new AABB(pos).expandTowards(
                be.getDirX() * 32.0,
                be.getDirY() * 32.0,
                be.getDirZ() * 32.0
        );
    }
}
