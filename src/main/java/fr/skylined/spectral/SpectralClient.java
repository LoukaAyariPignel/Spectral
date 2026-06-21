package fr.skylined.spectral;

import fr.skylined.spectral.component.ModComponents;
import fr.skylined.spectral.item.ModItems;
import fr.skylined.spectral.item.custom.GemItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = Spectral.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class SpectralClient {

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> {
            if (!stack.has(ModComponents.WAVE_LENGTH.get())) return -1;
            float wavelength = stack.get(ModComponents.WAVE_LENGTH.get());
            if (wavelength >= GemItem.MIN_WAVELENGTH && wavelength <= GemItem.MAX_WAVELENGTH && layer == 0) {
                return getColorFromWavelength(wavelength);
            }
            return -1;
        }, ModItems.GEM.get());
    }

    private static int getColorFromWavelength(float wavelength) {
        final float gamma = 0.80f;
        final int intensityMax = 255;
        float red = 0, green = 0, blue = 0;
        float factor;

        if (wavelength >= 380 && wavelength < 440) {
            red = -(wavelength - 440) / (440 - 380);
            green = 0.0f;
            blue = 1.0f;
        } else if (wavelength >= 440 && wavelength < 490) {
            red = 0.0f;
            green = (wavelength - 440) / (490 - 440);
            blue = 1.0f;
        } else if (wavelength >= 490 && wavelength < 510) {
            red = 0.0f;
            green = 1.0f;
            blue = -(wavelength - 510) / (510 - 490);
        } else if (wavelength >= 510 && wavelength < 580) {
            red = (wavelength - 510) / (580 - 510);
            green = 1.0f;
            blue = 0.0f;
        } else if (wavelength >= 580 && wavelength < 645) {
            red = 1.0f;
            green = -(wavelength - 645) / (645 - 580);
            blue = 0.0f;
        } else if (wavelength >= 645 && wavelength <= 780) {
            red = 1.0f;
            green = 0.0f;
            blue = 0.0f;
        }

        if (wavelength >= 380 && wavelength < 420) {
            factor = 0.3f + 0.7f * (wavelength - 380) / (420 - 380);
        } else if (wavelength >= 420 && wavelength < 701) {
            factor = 1.0f;
        } else if (wavelength >= 701 && wavelength < 781) {
            factor = 0.3f + 0.7f * (780 - wavelength) / (780 - 700);
        } else {
            factor = 0.0f;
        }

        if (red != 0) red = Math.round(intensityMax * Math.pow(red * factor, gamma));
        if (green != 0) green = Math.round(intensityMax * Math.pow(green * factor, gamma));
        if (blue != 0) blue = Math.round(intensityMax * Math.pow(blue * factor, gamma));

        return (0xFF << 24) | ((int) red << 16) | ((int) green << 8) | (int) blue;
    }
}
