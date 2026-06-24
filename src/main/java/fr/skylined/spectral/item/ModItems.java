package fr.skylined.spectral.item;

import fr.skylined.spectral.Spectral;
import fr.skylined.spectral.block.ModBlocks;
import fr.skylined.spectral.block.entity.LightBatteryBlockEntity;
import fr.skylined.spectral.item.custom.GemItem;
import fr.skylined.spectral.item.custom.LightBatteryItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Spectral.MOD_ID);

    public static final DeferredItem<GemItem>    GEM            = ITEMS.registerItem("gem", GemItem::new);
    public static final DeferredItem<Item>       RAW_CRYSTAL    = ITEMS.registerSimpleItem("raw_crystal");
    public static final DeferredItem<BlockItem>  RAW_CRYSTAL_ORE= ITEMS.registerSimpleBlockItem(ModBlocks.RAW_CRYSTAL_ORE);
    public static final DeferredItem<BlockItem>  PRISM_STAND    = ITEMS.registerSimpleBlockItem(ModBlocks.PRISM_STAND);
    public static final DeferredItem<BlockItem>  SOLAR_COLLECTOR= ITEMS.registerSimpleBlockItem(ModBlocks.SOLAR_COLLECTOR);
    public static final DeferredItem<BlockItem>  LIGHT_EMITTER  = ITEMS.registerSimpleBlockItem(ModBlocks.LIGHT_EMITTER);
    public static final DeferredItem<BlockItem>  CRYSTAL_FURNACE= ITEMS.registerSimpleBlockItem(ModBlocks.CRYSTAL_FURNACE);
    public static final DeferredItem<LightBatteryItem> LIGHT_BATTERY    = ITEMS.registerItem("light_battery",    p -> new LightBatteryItem(ModBlocks.LIGHT_BATTERY.get(),    LightBatteryBlockEntity.T1_CAPACITY, p));
    public static final DeferredItem<LightBatteryItem> LIGHT_BATTERY_T2 = ITEMS.registerItem("light_battery_t2", p -> new LightBatteryItem(ModBlocks.LIGHT_BATTERY_T2.get(), LightBatteryBlockEntity.T2_CAPACITY, p));
    public static final DeferredItem<LightBatteryItem> LIGHT_BATTERY_T3 = ITEMS.registerItem("light_battery_t3", p -> new LightBatteryItem(ModBlocks.LIGHT_BATTERY_T3.get(), LightBatteryBlockEntity.T3_CAPACITY, p));
    public static final DeferredItem<LightBatteryItem> LIGHT_BATTERY_T4 = ITEMS.registerItem("light_battery_t4", p -> new LightBatteryItem(ModBlocks.LIGHT_BATTERY_T4.get(), LightBatteryBlockEntity.T4_CAPACITY, p));
}
