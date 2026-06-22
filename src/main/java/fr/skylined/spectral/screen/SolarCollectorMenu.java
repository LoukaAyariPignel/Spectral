package fr.skylined.spectral.screen;

import fr.skylined.spectral.block.entity.SolarCollectorBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class SolarCollectorMenu extends AbstractContainerMenu {

    private final ContainerData data;

    public SolarCollectorMenu(int id, Inventory inv) {
        super(ModMenuTypes.SOLAR_COLLECTOR.get(), id);
        this.data = new SimpleContainerData(3);
        addDataSlots(data);
    }

    public SolarCollectorMenu(int id, Inventory inv, SolarCollectorBlockEntity be) {
        super(ModMenuTypes.SOLAR_COLLECTOR.get(), id);
        this.data = be.getContainerData();
        addDataSlots(data);
    }

    public int getStoredPhotons() { return data.get(0); }
    public int getMaxPhotons()    { return data.get(1); }
    public int getProduction()    { return data.get(2); }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
