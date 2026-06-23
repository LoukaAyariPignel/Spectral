package fr.skylined.spectral.screen;

import fr.skylined.spectral.block.entity.LightEmitterBlockEntity;
import fr.skylined.spectral.component.ModComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LightEmitterMenu extends AbstractContainerMenu {

    private final ContainerData data;

    // Slot 0 = gem, slots 1-27 = inventaire joueur, slots 28-36 = hotbar
    public LightEmitterMenu(int id, Inventory inv) {
        this(id, inv, new SimpleContainer(1), new SimpleContainerData(4));
    }

    public LightEmitterMenu(int id, Inventory inv, LightEmitterBlockEntity be) {
        this(id, inv, be.getGemContainer(), be.getContainerData());
    }

    private LightEmitterMenu(int id, Inventory playerInv, SimpleContainer gemContainer, ContainerData data) {
        super(ModMenuTypes.LIGHT_EMITTER.get(), id);
        this.data = data;
        addDataSlots(data);

        // Slot gem (centré dans le GUI, inner 80,52)
        // Gem slot centré (inner x=80, y=46 — border y=45-62)
        addSlot(new GemSlot(gemContainer, 0, 80, 49));

        // Inventaire joueur (3 rangées, inner y=90/108/126, border y=89/107/125)
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        // Hotbar (inner y=146, border y=145-162, 3px gris avant bordure PNG y=166)
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
    }

    public int getStoredPhotons() { return data.get(0); }
    public int getMaxPhotons()    { return data.get(1); }
    public boolean isEmitting()   { return data.get(2) == 1; }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    // Slot qui n'accepte que les gems (items ayant le composant WAVE_LENGTH)
    private static class GemSlot extends Slot {
        GemSlot(SimpleContainer c, int i, int x, int y) { super(c, i, x, y); }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.has(ModComponents.WAVE_LENGTH.get());
        }

        @Override
        public int getMaxStackSize(ItemStack stack) { return 1; }
    }
}
