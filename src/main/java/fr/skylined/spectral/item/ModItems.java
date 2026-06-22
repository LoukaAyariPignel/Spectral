package fr.skylined.spectral.item;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.item.custom.GemItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Spectral.MOD_ID);

    public static final DeferredItem<GemItem> GEM = ITEMS.registerItem("gem", GemItem::new);
}
