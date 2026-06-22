package fr.skylined.spectral.block;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.block.custom.LightEmitterBlock;
import fr.skylined.spectral.block.custom.PrismStandBlock;
import fr.skylined.spectral.block.custom.SolarCollectorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Spectral.MOD_ID);

    public static final DeferredBlock<Block> RAW_CRYSTAL_ORE = BLOCKS.registerSimpleBlock(
            "raw_crystal_ore",
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE)
    );

    public static final DeferredBlock<Block> PRISM_STAND = BLOCKS.registerBlock(
            "prism_stand",
            PrismStandBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()
    );

    public static final DeferredBlock<Block> SOLAR_COLLECTOR = BLOCKS.registerBlock(
            "solar_collector",
            SolarCollectorBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
    );

    public static final DeferredBlock<Block> LIGHT_EMITTER = BLOCKS.registerBlock(
            "light_emitter",
            LightEmitterBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(2.5f, 8.0f)
                    .sound(SoundType.METAL)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
    );
}
