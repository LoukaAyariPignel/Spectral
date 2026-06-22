package fr.skylined.spectral.screen;

import fr.skylined.spectral.block.entity.LightEmitterBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class LightEmitterMenu extends AbstractContainerMenu {

    private final ContainerData data;

    public LightEmitterMenu(int id, Inventory inv) {
        super(ModMenuTypes.LIGHT_EMITTER.get(), id);
        this.data = new SimpleContainerData(4);
        addDataSlots(data);
    }

    public LightEmitterMenu(int id, Inventory inv, LightEmitterBlockEntity be) {
        super(ModMenuTypes.LIGHT_EMITTER.get(), id);
        this.data = be.getContainerData();
        addDataSlots(data);
    }

    public int getStoredPhotons() { return data.get(0); }
    public int getMaxPhotons()    { return data.get(1); }
    public boolean isEmitting()   { return data.get(2) == 1; }

    public Direction getFacing() {
        int v = data.get(3);
        return Direction.from2DDataValue(v >= 0 && v < 4 ? v : 0);
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }
}
