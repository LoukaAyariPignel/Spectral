package fr.skylined.spectral.item;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.block.ModBlocks;
import fr.skylined.spectral.item.custom.GemItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Spectral.MOD_ID);

    public static final DeferredItem<GemItem> GEM = ITEMS.registerItem("gem", GemItem::new);
    public static final DeferredItem<Item> RAW_CRYSTAL = ITEMS.registerSimpleItem("raw_crystal");
    public static final DeferredItem<BlockItem> RAW_CRYSTAL_ORE = ITEMS.registerSimpleBlockItem(ModBlocks.RAW_CRYSTAL_ORE);
}
