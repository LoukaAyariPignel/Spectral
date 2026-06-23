package fr.skylined.spectral.block.entity;

import fr.skylined.spectral.block.custom.CrystalFurnaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.SimpleContainer;

import java.util.Optional;

public class CrystalFurnaceBlockEntity extends BlockEntity {

    public static final float OPTIMAL_WAVELENGTH = 700.0f;
    private static final int COOK_TIME = 200;

    private final SimpleContainer inventory = new SimpleContainer(2);
    private float receivedWavelength = 0f;
    private int beamActiveTicks = 0;
    private float cookProgress = 0f;

    public CrystalFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRYSTAL_FURNACE.get(), pos, state);
    }

    public void receiveBeam(float wavelength) {
        this.receivedWavelength = wavelength;
        this.beamActiveTicks = 3;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrystalFurnaceBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        boolean beamActive = be.beamActiveTicks > 0;
        if (be.beamActiveTicks > 0) be.beamActiveTicks--;

        float eff = beamActive ? calculateEfficiency(be.receivedWavelength) : 0f;
        ItemStack input = be.inventory.getItem(0);
        boolean wasLit = state.getValue(CrystalFurnaceBlock.LIT);

        if (beamActive && !input.isEmpty()) {
            Optional<RecipeHolder<SmeltingRecipe>> optRecipe = findSmeltingRecipe(serverLevel, input);
            if (optRecipe.isPresent()) {
                // Vitesse exactement proportionnelle à l'efficacité : 0→3× vanilla
                be.cookProgress += eff * 3f;
                if (be.cookProgress >= COOK_TIME) {
                    completeRecipe(be, serverLevel, optRecipe.get(), eff);
                }
                be.setChanged();
                if (!wasLit) level.setBlock(pos, state.setValue(CrystalFurnaceBlock.LIT, true), 3);
                return;
            }
        }

        // Pas d'input ou pas de recette : refroidissement
        if (be.cookProgress > 0f) {
            be.cookProgress = Math.max(0f, be.cookProgress - 2f);
            be.setChanged();
        }
        if (wasLit) level.setBlock(pos, state.setValue(CrystalFurnaceBlock.LIT, false), 3);
    }

    public static float calculateEfficiency(float wavelength) {
        float delta = Math.abs(wavelength - OPTIMAL_WAVELENGTH);
        if (delta >= 80f) return 0f; // hors de la plage utile 620–780 nm
        // Courbe en puissance : plate près de l'optimum, abrupte aux bords
        // eff = (1 - δ/80)^0.4  →  89% à ±20nm, 76% à ±40nm, 59% à ±60nm
        float t = 1f - delta / 80f;
        return (float) Math.pow(t, 0.4);
    }

    @SuppressWarnings("unchecked")
    private static Optional<RecipeHolder<SmeltingRecipe>> findSmeltingRecipe(ServerLevel level, ItemStack input) {
        return level.recipeAccess()
                .recipeMap()
                .byType(RecipeType.SMELTING)
                .stream()
                .filter(h -> h.value().matches(new SingleRecipeInput(input), level))
                .findFirst()
                .map(h -> (RecipeHolder<SmeltingRecipe>) h);
    }

    private static void completeRecipe(CrystalFurnaceBlockEntity be, ServerLevel level,
            RecipeHolder<SmeltingRecipe> recipe, float efficiency) {
        ItemStack result = recipe.value().assemble(
                new SingleRecipeInput(be.inventory.getItem(0)));

        if (efficiency >= 0.9f && level.getRandom().nextFloat() < 0.10f) {
            result = result.copyWithCount(result.getCount() * 2);
        }

        ItemStack existing = be.inventory.getItem(1);
        if (existing.isEmpty()) {
            be.inventory.setItem(1, result.copy());
        } else if (ItemStack.isSameItemSameComponents(existing, result)
                && existing.getCount() < existing.getMaxStackSize()) {
            existing.grow(result.getCount());
        } else {
            return; // output full
        }

        be.inventory.getItem(0).shrink(1);
        be.cookProgress = 0;
        be.setChanged();
    }

    public SimpleContainer getInventory() { return inventory; }
    public float getReceivedWavelength()  { return receivedWavelength; }
    public boolean isBeamActive()         { return beamActiveTicks > 0; }
    public int getCookProgress()          { return (int) cookProgress; }
    public int getCookTimeTotal()         { return COOK_TIME; }

    public ContainerData getContainerData() {
        return new ContainerData() {
            @Override public int get(int i) {
                return switch (i) {
                    case 0 -> (int)(cookProgress * 10f);
                    case 1 -> COOK_TIME * 10;
                    case 2 -> (int)(receivedWavelength * 10f);
                    case 3 -> Math.round(calculateEfficiency(receivedWavelength) * 100f);
                    case 4 -> beamActiveTicks > 0 ? 1 : 0;
                    case 5 -> (int)(OPTIMAL_WAVELENGTH * 10f);
                    default -> 0;
                };
            }
            @Override public void set(int i, int v) {}
            @Override public int getCount() { return 6; }
        };
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (getLevel() != null) getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!inventory.getItem(0).isEmpty()) output.store("slot_in",  ItemStack.CODEC, inventory.getItem(0));
        if (!inventory.getItem(1).isEmpty()) output.store("slot_out", ItemStack.CODEC, inventory.getItem(1));
        output.putFloat("wavelength", receivedWavelength);
        output.putFloat("progress", cookProgress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.setItem(0, input.read("slot_in",  ItemStack.CODEC).orElse(ItemStack.EMPTY));
        inventory.setItem(1, input.read("slot_out", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        receivedWavelength = input.getFloatOr("wavelength", 0f);
        cookProgress = input.getFloatOr("progress", 0f);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
