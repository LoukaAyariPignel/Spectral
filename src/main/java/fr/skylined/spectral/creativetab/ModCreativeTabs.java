package fr.skylined.spectral.creativetab;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Spectral.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SPECTRAL_TAB =
            CREATIVE_MODE_TABS.register("spectral_tab",
                    () -> CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.spectral"))
                            .icon(() -> new ItemStack(ModItems.GEM.get()))
                            .displayItems((params, output) -> {
                                output.accept(ModItems.RAW_CRYSTAL.get());
                                output.accept(ModItems.GEM.get());
                            })
                            .build()
            );
}
