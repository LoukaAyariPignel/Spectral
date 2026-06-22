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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PrismStandBlockEntity>> PRISM_STAND =
            BLOCK_ENTITIES.register("prism_stand", () ->
                    BlockEntityType.Builder.of(PrismStandBlockEntity::new, ModBlocks.PRISM_STAND.get()).build(null)
            );
}
