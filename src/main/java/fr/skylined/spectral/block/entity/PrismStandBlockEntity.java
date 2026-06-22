package fr.skylined.spectral.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PrismStandBlockEntity extends BlockEntity {

    private ItemStack storedItem = ItemStack.EMPTY;

    public PrismStandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PRISM_STAND.get(), pos, state);
    }

    public ItemStack getStoredItem() {
        return storedItem;
    }

    public void setStoredItem(ItemStack stack) {
        this.storedItem = stack;
        setChanged();
    }

    public boolean isEmpty() {
        return storedItem.isEmpty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!storedItem.isEmpty()) {
            tag.put("item", storedItem.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("item")) {
            storedItem = ItemStack.parse(registries, tag.getCompound("item")).orElse(ItemStack.EMPTY);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
