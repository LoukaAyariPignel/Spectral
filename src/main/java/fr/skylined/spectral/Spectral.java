package fr.skylined.spectral;

import fr.skylined.spectral.block.ModBlocks;
import fr.skylined.spectral.block.entity.ModBlockEntities;
import fr.skylined.spectral.component.ModComponents;
import fr.skylined.spectral.creativetab.ModCreativeTabs;
import fr.skylined.spectral.item.ModItems;
import fr.skylined.spectral.recipe.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Spectral.MOD_ID)
public class Spectral {
    public static final String MOD_ID = "spectral";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Spectral(IEventBus modEventBus) {
        ModComponents.COMPONENTS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
    }
}
