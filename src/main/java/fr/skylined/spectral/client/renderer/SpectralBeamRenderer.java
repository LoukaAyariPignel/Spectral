package fr.skylined.spectral.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

/**
 * Beam renderer that uses a translucent render type for both inner beam and glow,
 * so the alpha channel of colorARGB is actually respected.
 * (Vanilla BeaconRenderer.submitBeaconBeam uses OPAQUE for the inner beam, ignoring alpha.)
 */
public final class SpectralBeamRenderer {

    private SpectralBeamRenderer() {}

    /**
     * Renders a translucent beam segment.
     * The PoseStack must already be positioned at the beam start point
     * and oriented so that local Y points in the beam direction.
     * No internal centering translate is performed here.
     */
    public static void submitTranslucentBeam(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            Identifier texture,
            float textureScale,
            float animTime,
            int startY, int height,
            int colorARGB,
            float innerRadius, float glowRadius) {

        int endY = startY + height;

        poseStack.pushPose();

        float f = startY >= 0 ? animTime : -animTime;
        float scroll = Mth.frac(f * 0.2f - Mth.floor(f * 0.1f));
        float vBot = scroll - 1.0f;

        // Inner beam — rotated diamond, translucent (vanilla uses OPAQUE here)
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(animTime * 2.25f - 45f));

        float vTopInner = (endY - startY) * textureScale * (0.5f / innerRadius) + vBot;
        var innerType = RenderTypes.beaconBeam(texture, true);
        final int ic = colorARGB;
        final float ri = innerRadius;
        final int sy = startY, ey = endY;
        final float vbi = vBot, vti = vTopInner;

        collector.submitCustomGeometry(poseStack, innerType, (pose, consumer) ->
            renderPart(pose, consumer, ic, sy, ey,
                0f, ri, ri, 0f, -ri, 0f, 0f, -ri,
                0f, 1f, vti, vbi)
        );

        poseStack.popPose();

        // Outer glow — axis-aligned square, translucent, lower alpha (same ratio as vanilla: 32/255)
        float vTopGlow = (endY - startY) * textureScale + vBot;
        int innerAlpha = ARGB.alpha(colorARGB);
        int glowAlpha  = (innerAlpha * 32) / 255;
        int glowColor  = ARGB.color(glowAlpha, colorARGB);
        var glowType   = RenderTypes.beaconBeam(texture, true);
        final int gc = glowColor;
        final float rg = glowRadius;
        final float vbg = vBot, vtg = vTopGlow;

        collector.submitCustomGeometry(poseStack, glowType, (pose, consumer) ->
            renderPart(pose, consumer, gc, sy, ey,
                -rg, -rg, rg, -rg, -rg, rg, rg, rg,
                0f, 1f, vtg, vbg)
        );

        poseStack.popPose();
    }

    // Draws 4 quads forming a prism face (same order as BeaconRenderer.renderPart)
    private static void renderPart(PoseStack.Pose pose, VertexConsumer consumer,
            int color, int y0, int y1,
            float x1, float z1, float x2, float z2,
            float x3, float z3, float x4, float z4,
            float uRight, float uLeft, float vTop, float vBot) {
        renderQuad(pose, consumer, color, y0, y1, x1, z1, x2, z2, uRight, uLeft, vTop, vBot);
        renderQuad(pose, consumer, color, y0, y1, x4, z4, x3, z3, uRight, uLeft, vTop, vBot);
        renderQuad(pose, consumer, color, y0, y1, x2, z2, x4, z4, uRight, uLeft, vTop, vBot);
        renderQuad(pose, consumer, color, y0, y1, x3, z3, x1, z1, uRight, uLeft, vTop, vBot);
    }

    // One rectangular face of the beam (same vertex layout as BeaconRenderer.renderQuad)
    private static void renderQuad(PoseStack.Pose pose, VertexConsumer consumer,
            int color, int y0, int y1,
            float x1, float z1, float x2, float z2,
            float uRight, float uLeft, float vTop, float vBot) {
        addVertex(pose, consumer, color, y1, x1, z1, uLeft,  vTop);
        addVertex(pose, consumer, color, y0, x1, z1, uLeft,  vBot);
        addVertex(pose, consumer, color, y0, x2, z2, uRight, vBot);
        addVertex(pose, consumer, color, y1, x2, z2, uRight, vTop);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer consumer,
            int color, int y, float x, float z, float u, float v) {
        consumer.addVertex(pose, x, (float) y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(0)        // OverlayTexture.NO_OVERLAY
                .setLight(0xF000F0)   // LightTexture.FULL_BRIGHT
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }
}
