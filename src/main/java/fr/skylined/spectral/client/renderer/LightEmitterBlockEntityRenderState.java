package fr.skylined.spectral.client.renderer;

import fr.skylined.spectral.beam.BeamSegment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import java.util.List;

public class LightEmitterBlockEntityRenderState extends BlockEntityRenderState {
    public List<BeamSegment> segments = List.of();
    /** Normalized 3D beam direction (set by the block entity, any angle). */
    public float dirX = 0f, dirY = 0f, dirZ = 1f;
    public long gameTime;
    public float partialTick;
    public float beamAlpha = 1f;
}
