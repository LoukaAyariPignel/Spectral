package fr.skylined.spectral.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.skylined.spectral.block.entity.CrystalLensBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CrystalLensBlockEntityRenderer implements BlockEntityRenderer<CrystalLensBlockEntity, CrystalLensBlockEntityRenderState> {

    private final ItemModelResolver itemModelResolver;

    public CrystalLensBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public CrystalLensBlockEntityRenderState createRenderState() {
        return new CrystalLensBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(CrystalLensBlockEntity be, CrystalLensBlockEntityRenderState state,
            float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        itemModelResolver.updateForTopItem(
                state.itemRenderState,
                be.getStoredItem(),
                ItemDisplayContext.FIXED,
                be.getLevel(),
                null,
                (int) be.getBlockPos().asLong()
        );
    }

    @Override
    public void submit(CrystalLensBlockEntityRenderState state, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.itemRenderState.isEmpty()) return;

        poseStack.pushPose();
        // Centre de l'arceau : X=8/16=0.5, Y=(2+11)/2=6.5/16≈0.406, Z=8/16=0.5
        poseStack.translate(0.5, 0.424, 0.5);
        poseStack.scale(0.6f, 0.6f, 0.6f);

        state.itemRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
