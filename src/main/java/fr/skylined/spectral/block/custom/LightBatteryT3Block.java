package fr.skylined.spectral.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class LightBatteryT3Block extends LightBatteryBlock {
    public static final MapCodec<LightBatteryT3Block> CODEC = simpleCodec(LightBatteryT3Block::new);
    public LightBatteryT3Block(BlockBehaviour.Properties p) { super(p); }
    @Override protected MapCodec<? extends LightBatteryBlock> codec() { return CODEC; }
}
