package fr.skylined.spectral.item.custom;

import fr.skylined.spectral.component.ModComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class GemItem extends Item {

    public static final float MIN_WAVELENGTH = 380f;
    public static final float MAX_WAVELENGTH = 780f;

    public GemItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (stack.has(ModComponents.WAVE_LENGTH.get())) {
            float wavelength = stack.getOrDefault(ModComponents.WAVE_LENGTH.get(), MIN_WAVELENGTH);
            tooltip.accept(Component.literal(String.format("%.1f nm", wavelength))
                    .withStyle(style -> style.withColor(0x646464)));
        }
    }
}
