package fr.skylined.spectral.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PrismStandBlockEntityRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public long gameTime;
    public float partialTick;
}
