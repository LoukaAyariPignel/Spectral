package fr.skylined.spectral.block.entity;

import fr.skylined.spectral.block.custom.LightEmitterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LightEmitterBlockEntity extends BlockEntity {

    public static final long MAX_PHOTONS = 1000L;
    private static final long EMISSION_THRESHOLD = 5L;
    private static final long EMISSION_COST = 5L;

    private long storedPhotons = 0;
    private boolean emitting = false;

    public LightEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIGHT_EMITTER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LightEmitterBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        boolean wasEmitting = be.emitting;

        if (be.storedPhotons >= EMISSION_THRESHOLD) {
            be.storedPhotons -= EMISSION_COST;
            be.emitting = true;
        } else {
            be.emitting = false;
        }

        if (be.emitting) {
            Direction facing = state.getValue(LightEmitterBlock.FACING);
            spawnBeamParticle(serverLevel, pos, facing);
        }

        if (be.emitting != wasEmitting) {
            be.setChanged();
        }
    }

    private static void spawnBeamParticle(ServerLevel level, BlockPos pos, Direction facing) {
        double cx = pos.getX() + 0.5 + facing.getStepX() * 0.7;
        double cy = pos.getY() + 0.5 + facing.getStepY() * 0.7;
        double cz = pos.getZ() + 0.5 + facing.getStepZ() * 0.7;
        double vx = facing.getStepX() * 0.15;
        double vy = facing.getStepY() * 0.15;
        double vz = facing.getStepZ() * 0.15;
        level.sendParticles(ParticleTypes.END_ROD, cx, cy, cz, 1, vx, vy, vz, 0.01);
    }

    public void addPhotons(long amount) {
        storedPhotons = Math.min(MAX_PHOTONS, storedPhotons + amount);
        setChanged();
    }

    public long getStoredPhotons() { return storedPhotons; }
    public boolean isEmitting() { return emitting; }

    @Override
    public void setChanged() {
        super.setChanged();
        if (getLevel() != null) {
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("photons", storedPhotons);
        output.putBoolean("emitting", emitting);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedPhotons = input.getLongOr("photons", 0L);
        emitting = input.getBooleanOr("emitting", false);
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
