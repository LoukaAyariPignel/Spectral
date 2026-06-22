package fr.skylined.spectral.block.entity;

import fr.skylined.spectral.recipe.ModRecipes;
import fr.skylined.spectral.recipe.PrismStandRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Optional;

public class PrismStandBlockEntity extends BlockEntity {

    private ItemStack storedItem = ItemStack.EMPTY;
    private int progress = 0;

    public PrismStandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRISM_STAND.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PrismStandBlockEntity be) {
        if (be.storedItem.isEmpty()) {
            be.resetProgress();
            return;
        }

        int skyLight = level.getBrightness(LightLayer.SKY, pos.above());
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos.above());
        int totalLight = Math.max(skyLight, blockLight);

        if (!(level instanceof ServerLevel serverLevel)) return;
        Optional<PrismStandRecipe> recipe = serverLevel.recipeAccess()
                .recipeMap()
                .byType(ModRecipes.PRISM_STAND.get())
                .stream()
                .map(RecipeHolder::value)
                .filter(r -> r.matches(be.storedItem, totalLight))
                .findFirst();

        if (recipe.isEmpty()) {
            be.resetProgress();
            return;
        }

        PrismStandRecipe r = recipe.get();

        if (r.result().useSkyLight()) {
            long dayTime = level.getOverworldClockTime() % 24000L;
            // MC 26.1 : nuit entre 12600 et 23401 (Timelines.java)
            if (dayTime >= 12600 && dayTime <= 23401) {
                be.resetProgress();
                return;
            }
        }

        be.progress++;

        if (be.progress % 10 == 0) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                    2, 0.15, 0.05, 0.15, 0.02);
        }

        if (be.progress >= r.processingTime()) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    20, 0.3, 0.3, 0.3, 0.05);
            ItemStack result = r.result().assemble(level);
            if (!result.isEmpty()) {
                be.storedItem = result;
                be.setChanged();
            }
            be.progress = 0;
        }
    }

    private void resetProgress() {
        if (progress != 0) {
            progress = 0;
        }
    }

    public int getProgress() { return progress; }

    public ItemStack getStoredItem() { return storedItem; }

    public void setStoredItem(ItemStack stack) {
        this.storedItem = stack;
        this.progress = 0;
        setChanged();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (getLevel() != null) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isEmpty() { return storedItem.isEmpty(); }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!storedItem.isEmpty()) {
            output.store("item", ItemStack.CODEC, storedItem);
        }
        output.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedItem = input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        progress = input.getIntOr("progress", 0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
