package fr.skylined.spectral.screen;

import fr.skylined.spectral.block.entity.LightBatteryBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class LightBatteryMenu extends AbstractContainerMenu {

    private final ContainerData data;

    public LightBatteryMenu(int id, Inventory inv) {
        super(ModMenuTypes.LIGHT_BATTERY.get(), id);
        this.data = new SimpleContainerData(5);
        addDataSlots(data);
    }

    public LightBatteryMenu(int id, Inventory inv, LightBatteryBlockEntity be) {
        super(ModMenuTypes.LIGHT_BATTERY.get(), id);
        this.data = be.getContainerData();
        addDataSlots(data);
    }

    /** Reconstitue le long depuis les deux halves int. */
    public long getStoredPhotons() {
        return (data.get(0) & 0x7FFFL) | ((long) data.get(1) << 15);
    }

    public long getMaxPhotons() {
        return (data.get(2) & 0x7FFFL) | ((long) data.get(3) << 15);
    }

    public boolean isReceiving() { return data.get(4) == 1; }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
