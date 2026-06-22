package fr.skylined.spectral.client.renderer;

import fr.skylined.spectral.beam.BeamSegment;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import java.util.List;

public class LightEmitterBlockEntityRenderState extends BlockEntityRenderState {
    public List<BeamSegment> segments = List.of();
    public Direction facing = Direction.NORTH;
    public long gameTime;
    public float partialTick;
}
