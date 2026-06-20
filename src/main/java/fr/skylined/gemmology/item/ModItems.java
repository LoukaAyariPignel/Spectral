package fr.skylined.gemmology.item;

import fr.skylined.gemmology.Gemmology;
import fr.skylined.gemmology.item.custom.GemItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item GEM = registerItem("gem", new GemItem(new Item.Settings()/*.component(ModComponents.WAVE_LENGTH, GemItem.MIN_WAVELENGTH)*/));


    private static Item registerItem(String name, Item item){
        return Registry.register(Registries.ITEM, Identifier.of(Gemmology.MOD_ID, name), item);
    }

    public static void registerModItems(){
        Gemmology.LOGGER.info("Registering items for " + Gemmology.MOD_ID + ".");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.add(GEM);
        });

        Gemmology.LOGGER.info("Done.");
    }
}
