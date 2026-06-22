package fr.skylined.spectral.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.skylined.spectral.block.entity.PrismStandBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jspecify.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class PrismStandBlockEntityRenderer implements BlockEntityRenderer<PrismStandBlockEntity, PrismStandBlockEntityRenderState> {

    private final ItemModelResolver itemModelResolver;

    public PrismStandBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemModelResolver = ctx.itemModelResolver();
    }

    @Override
    public PrismStandBlockEntityRenderState createRenderState() {
        return new PrismStandBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(PrismStandBlockEntity be, PrismStandBlockEntityRenderState state,
            float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        state.gameTime = be.getLevel() != null ? be.getLevel().getGameTime() : 0;
        state.partialTick = partialTicks;
        itemModelResolver.updateForTopItem(
                state.itemRenderState,
                be.getStoredItem(),
                ItemDisplayContext.GROUND,
                be.getLevel(),
                null,
                (int) be.getBlockPos().asLong()
        );
    }

    @Override
    public void submit(PrismStandBlockEntityRenderState state, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.itemRenderState.isEmpty()) return;

        float angle = (state.gameTime + state.partialTick) * 2.5f;
        float bob = (float) Math.sin((state.gameTime + state.partialTick) * 0.1f) * 0.05f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.92 + bob, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.scale(0.5f, 0.5f, 0.5f);

        state.itemRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
