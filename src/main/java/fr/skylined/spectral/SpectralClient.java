package fr.skylined.spectral;

import fr.skylined.spectral.block.entity.LightEmitterBlockEntity;
import fr.skylined.spectral.block.entity.ModBlockEntities;
import fr.skylined.spectral.block.entity.CrystalLensBlockEntity;
import fr.skylined.spectral.client.color.WavelengthTintSource;
import fr.skylined.spectral.client.renderer.CrystalLensBlockEntityRenderState;
import fr.skylined.spectral.client.renderer.CrystalLensBlockEntityRenderer;
import fr.skylined.spectral.client.renderer.LightEmitterBlockEntityRenderState;
import fr.skylined.spectral.client.renderer.LightEmitterBlockEntityRenderer;
import fr.skylined.spectral.screen.CrystalFurnaceScreen;
import fr.skylined.spectral.screen.LightBatteryScreen;
import fr.skylined.spectral.screen.LightEmitterScreen;
import fr.skylined.spectral.screen.ModMenuTypes;
import fr.skylined.spectral.screen.SolarCollectorScreen;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Spectral.MOD_ID, value = Dist.CLIENT)
public class SpectralClient {

    @SubscribeEvent
    public static void onRegisterItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(Identifier.fromNamespaceAndPath(Spectral.MOD_ID, "wavelength"), WavelengthTintSource.CODEC);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.CRYSTAL_LENS.get(),
                (BlockEntityRendererProvider<CrystalLensBlockEntity, CrystalLensBlockEntityRenderState>) CrystalLensBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LIGHT_EMITTER.get(),
                (BlockEntityRendererProvider<LightEmitterBlockEntity, LightEmitterBlockEntityRenderState>) LightEmitterBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SOLAR_COLLECTOR.get(), SolarCollectorScreen::new);
        event.register(ModMenuTypes.LIGHT_EMITTER.get(), LightEmitterScreen::new);
        event.register(ModMenuTypes.CRYSTAL_FURNACE.get(), CrystalFurnaceScreen::new);
        event.register(ModMenuTypes.LIGHT_BATTERY.get(), LightBatteryScreen::new);
    }
}
