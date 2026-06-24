package fr.skylined.spectral.block;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.block.custom.CrystalFurnaceBlock;
import fr.skylined.spectral.block.custom.LightBatteryBlock;
import fr.skylined.spectral.block.custom.LightBatteryT2Block;
import fr.skylined.spectral.block.custom.LightBatteryT3Block;
import fr.skylined.spectral.block.custom.LightBatteryT4Block;
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
            "prism_stand", PrismStandBlock::new,
            () -> BlockBehaviour.Properties.of().strength(1.5f,6f).sound(SoundType.STONE).noOcclusion()
    );
    public static final DeferredBlock<Block> SOLAR_COLLECTOR = BLOCKS.registerBlock(
            "solar_collector", SolarCollectorBlock::new,
            () -> BlockBehaviour.Properties.of().strength(2f,6f).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> LIGHT_EMITTER = BLOCKS.registerBlock(
            "light_emitter", LightEmitterBlock::new,
            () -> BlockBehaviour.Properties.of().strength(2.5f,8f).sound(SoundType.METAL).noOcclusion().requiresCorrectToolForDrops()
    );
    public static final DeferredBlock<Block> CRYSTAL_FURNACE = BLOCKS.registerBlock(
            "crystal_furnace", CrystalFurnaceBlock::new,
            () -> BlockBehaviour.Properties.of().strength(3f,8f).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()
    );
    private static BlockBehaviour.Properties batteryProps() {
        return BlockBehaviour.Properties.of().strength(3f,8f).sound(SoundType.METAL).requiresCorrectToolForDrops();
    }
    public static final DeferredBlock<Block> LIGHT_BATTERY    = BLOCKS.registerBlock("light_battery",    LightBatteryBlock::new,   ModBlocks::batteryProps);
    public static final DeferredBlock<Block> LIGHT_BATTERY_T2 = BLOCKS.registerBlock("light_battery_t2", LightBatteryT2Block::new, ModBlocks::batteryProps);
    public static final DeferredBlock<Block> LIGHT_BATTERY_T3 = BLOCKS.registerBlock("light_battery_t3", LightBatteryT3Block::new, ModBlocks::batteryProps);
    public static final DeferredBlock<Block> LIGHT_BATTERY_T4 = BLOCKS.registerBlock("light_battery_t4", LightBatteryT4Block::new, ModBlocks::batteryProps);
}
