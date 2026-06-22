package fr.skylined.spectral.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.skylined.spectral.block.entity.PrismStandBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class PrismStandBlockEntityRenderer implements BlockEntityRenderer<PrismStandBlockEntity> {

    public PrismStandBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(PrismStandBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        ItemStack item = be.getStoredItem();
        if (item.isEmpty()) return;

        long time = be.getLevel() != null ? be.getLevel().getGameTime() : 0;
        float angle = (time + partialTick) * 2.5f;
        float bob = (float) Math.sin((time + partialTick) * 0.1f) * 0.05f;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.92 + bob, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.scale(0.5f, 0.5f, 0.5f);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(item, ItemDisplayContext.GROUND,
                combinedLight, combinedOverlay, poseStack, bufferSource,
                be.getLevel(), (int) be.getBlockPos().asLong());

        poseStack.popPose();
    }
}
