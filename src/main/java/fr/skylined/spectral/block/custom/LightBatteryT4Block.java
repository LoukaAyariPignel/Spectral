package fr.skylined.spectral.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LightBatteryT4Block extends LightBatteryBlock {
    public static final MapCodec<LightBatteryT4Block> CODEC = simpleCodec(LightBatteryT4Block::new);
    public LightBatteryT4Block(BlockBehaviour.Properties p) { super(p); }
    @Override protected MapCodec<? extends LightBatteryBlock> codec() { return CODEC; }
}
