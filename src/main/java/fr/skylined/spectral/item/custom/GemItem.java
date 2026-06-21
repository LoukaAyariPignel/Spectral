package fr.skylined.spectral.item.custom;

import fr.skylined.spectral.component.ModComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GemItem extends Item {

    public static final float MIN_WAVELENGTH = 380f;
    public static final float MAX_WAVELENGTH = 780f;

    public GemItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        if (!level.isClientSide()) {
            initializeWavelength(stack);
        }
    }

    private void initializeWavelength(ItemStack stack) {
        if (!stack.has(ModComponents.WAVE_LENGTH.get())) {
            float randomWavelength = MIN_WAVELENGTH + (MAX_WAVELENGTH - MIN_WAVELENGTH) * ThreadLocalRandom.current().nextFloat();
            stack.set(ModComponents.WAVE_LENGTH.get(), randomWavelength);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (stack.has(ModComponents.WAVE_LENGTH.get())) {
            float wavelength = stack.getOrDefault(ModComponents.WAVE_LENGTH.get(), MIN_WAVELENGTH);
            tooltipComponents.add(Component.literal(String.format("%.1f nm", wavelength))
                    .withStyle(style -> style.withColor(0x646464)));
        }
    }
}
