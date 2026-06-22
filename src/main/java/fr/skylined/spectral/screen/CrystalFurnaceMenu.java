package fr.skylined.spectral.screen;

import fr.skylined.spectral.block.entity.CrystalFurnaceBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CrystalFurnaceMenu extends AbstractContainerMenu {

    private final ContainerData data;

    public CrystalFurnaceMenu(int id, Inventory inv) {
        this(id, inv, new net.minecraft.world.SimpleContainer(2), new SimpleContainerData(5));
    }

    public CrystalFurnaceMenu(int id, Inventory inv, CrystalFurnaceBlockEntity be) {
        this(id, inv, be.getInventory(), be.getContainerData());
    }

    private CrystalFurnaceMenu(int id, Inventory playerInv, Container container, ContainerData data) {
        super(ModMenuTypes.CRYSTAL_FURNACE.get(), id);
        this.data = data;
        addDataSlots(data);

        // Match classic furnace layout from the GUI texture
        addSlot(new Slot(container, 0, 56, 17));          // input  — matches texture slot
        addSlot(new OutputSlot(container, 1, 116, 35));   // output — matches texture slot

        // Player inventory
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        // Hotbar
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
    }

    public int   getCookProgress()  { return data.get(0); }
    public int   getCookTimeTotal() { return Math.max(1, data.get(1)); }
    public float getWavelength()    { return data.get(2) / 10f; }
    public int   getEfficiencyPct() { return data.get(3); }
    public boolean isBeamActive()   { return data.get(4) == 1; }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < 2) {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    private static class OutputSlot extends Slot {
        OutputSlot(Container c, int i, int x, int y) { super(c, i, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
    }
}
