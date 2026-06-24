package fr.skylined.spectral.component;

import com.mojang.serialization.Codec;
import fr.skylined.spectral.Spectral;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Spectral.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> WAVE_LENGTH =
            COMPONENTS.register("wave_length",
                    () -> DataComponentType.<Float>builder().persistent(Codec.FLOAT).build()
            );

    /** Photons stockés dans une Light Battery — persistés dans l'item lors du drop. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> STORED_PHOTONS =
            COMPONENTS.register("stored_photons",
                    () -> DataComponentType.<Long>builder().persistent(Codec.LONG).build()
            );
}
