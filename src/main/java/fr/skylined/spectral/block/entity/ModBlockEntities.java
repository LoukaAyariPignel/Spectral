package fr.skylined.spectral.block.entity;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Spectral.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalLensBlockEntity>> CRYSTAL_LENS =
            BLOCK_ENTITIES.register("crystal_lens", () ->
                    new BlockEntityType<>(CrystalLensBlockEntity::new, ModBlocks.CRYSTAL_LENS.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SolarCollectorBlockEntity>> SOLAR_COLLECTOR =
            BLOCK_ENTITIES.register("solar_collector", () ->
                    new BlockEntityType<>(SolarCollectorBlockEntity::new, ModBlocks.SOLAR_COLLECTOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightEmitterBlockEntity>> LIGHT_EMITTER =
            BLOCK_ENTITIES.register("light_emitter", () ->
                    new BlockEntityType<>(LightEmitterBlockEntity::new, ModBlocks.LIGHT_EMITTER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrystalFurnaceBlockEntity>> CRYSTAL_FURNACE =
            BLOCK_ENTITIES.register("crystal_furnace", () ->
                    new BlockEntityType<>(CrystalFurnaceBlockEntity::new, ModBlocks.CRYSTAL_FURNACE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LightBatteryBlockEntity>> LIGHT_BATTERY =
            BLOCK_ENTITIES.register("light_battery", () ->
                    new BlockEntityType<>(LightBatteryBlockEntity::new,
                            ModBlocks.LIGHT_BATTERY.get(),
                            ModBlocks.LIGHT_BATTERY_T2.get(),
                            ModBlocks.LIGHT_BATTERY_T3.get(),
                            ModBlocks.LIGHT_BATTERY_T4.get()));
}
