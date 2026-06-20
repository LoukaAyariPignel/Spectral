package fr.skylined.gemmology.item.custom;


import fr.skylined.gemmology.component.ModComponents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GemItem extends Item {

    public static final float MIN_WAVELENGTH = 380f;
    public static final float MAX_WAVELENGTH = 780f;



    public GemItem(Settings settings) {
        super(settings);
    }

    // Initialisation de la longueur d'onde lors du craft
    @Override
    public void onCraft(ItemStack stack, World world) {
        super.onCraft(stack, world);
        if(!world.isClient()){
            initializeWavelength(stack);
        }
    }

    // Initialiser la longueur d'onde de manière aléatoire entre 380 nm et 750 nm
    private void initializeWavelength(ItemStack stack) {
        if (!stack.contains(ModComponents.WAVE_LENGTH)) {
            float randomWavelength = MIN_WAVELENGTH + (MAX_WAVELENGTH - MIN_WAVELENGTH) * ThreadLocalRandom.current().nextFloat();
            stack.set(ModComponents.WAVE_LENGTH, randomWavelength);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        if (stack.contains(ModComponents.WAVE_LENGTH)) {
            float wavelength = stack.getOrDefault(ModComponents.WAVE_LENGTH, MIN_WAVELENGTH);
            tooltip.add(Text.literal(String.format("%.1f nm", wavelength)).styled(style -> style.withColor(0x646464)));
        }
    }

}