package fr.skylined.spectral.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LightBatteryT2Block extends LightBatteryBlock {
    public static final MapCodec<LightBatteryT2Block> CODEC = simpleCodec(LightBatteryT2Block::new);
    public LightBatteryT2Block(BlockBehaviour.Properties p) { super(p); }
    @Override protected MapCodec<? extends LightBatteryBlock> codec() { return CODEC; }
}
