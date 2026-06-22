package fr.skylined.spectral;

import fr.skylined.spectral.client.color.WavelengthTintSource;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = Spectral.MOD_ID, value = Dist.CLIENT)
public class SpectralClient {

    @SubscribeEvent
    public static void onRegisterItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "wavelength"), WavelengthTintSource.CODEC);
    }
}
