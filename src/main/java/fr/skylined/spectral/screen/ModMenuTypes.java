package fr.skylined.spectral.screen;

import fr.skylined.spectral.Spectral;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, Spectral.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<SolarCollectorMenu>> SOLAR_COLLECTOR =
            MENU_TYPES.register("solar_collector",
                    () -> IMenuTypeExtension.create((id, inv, buf) -> new SolarCollectorMenu(id, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<LightEmitterMenu>> LIGHT_EMITTER =
            MENU_TYPES.register("light_emitter",
                    () -> IMenuTypeExtension.create((id, inv, buf) -> new LightEmitterMenu(id, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<CrystalFurnaceMenu>> CRYSTAL_FURNACE =
            MENU_TYPES.register("crystal_furnace",
                    () -> IMenuTypeExtension.create((id, inv, buf) -> new CrystalFurnaceMenu(id, inv)));

    public static final DeferredHolder<MenuType<?>, MenuType<LightBatteryMenu>> LIGHT_BATTERY =
            MENU_TYPES.register("light_battery",
                    () -> IMenuTypeExtension.create((id, inv, buf) -> new LightBatteryMenu(id, inv)));
}
