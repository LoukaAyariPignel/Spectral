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

    // Client constructor
    public CrystalFurnaceMenu(int id, Inventory inv) {
        this(id, inv, new net.minecraft.world.SimpleContainer(2), new SimpleContainerData(5));
    }

    // Server constructor
    public CrystalFurnaceMenu(int id, Inventory inv, CrystalFurnaceBlockEntity be) {
        this(id, inv, be.getInventory(), be.getContainerData());
    }

    private CrystalFurnaceMenu(int id, Inventory playerInv, Container container, ContainerData data) {
        super(ModMenuTypes.CRYSTAL_FURNACE.get(), id);
        this.data = data;
        addDataSlots(data);

        // Furnace slots
        addSlot(new Slot(container, 0, 44, 35));  // input
        addSlot(new OutputSlot(container, 1, 116, 35)); // output (no place)

        // Player inventory (3 rows)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public int getCookProgress()      { return data.get(0); }
    public int getCookTimeTotal()     { return Math.max(1, data.get(1)); }
    public float getWavelength()      { return data.get(2) / 10f; }
    public int getEfficiencyPct()     { return data.get(3); }
    public boolean isBeamActive()     { return data.get(4) == 1; }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return result;

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < 2) {
            // From furnace → player inventory
            if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            // From player → furnace input (slot 0)
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return result;
    }

    private static class OutputSlot extends Slot {
        OutputSlot(Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return false; }
    }
}
