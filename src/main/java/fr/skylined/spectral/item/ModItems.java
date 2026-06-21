package fr.skylined.spectral.item;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.item.custom.GemItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, Spectral.MOD_ID);

    public static final DeferredHolder<Item, GemItem> GEM =
            ITEMS.register("gem", () -> new GemItem(new Item.Properties()));
}
