package fr.skylined.spectral.block.entity;

import fr.skylined.spectral.block.custom.LightBatteryBlock;
import fr.skylined.spectral.block.custom.LightBatteryT2Block;
import fr.skylined.spectral.block.custom.LightBatteryT3Block;
import fr.skylined.spectral.block.custom.LightBatteryT4Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class LightBatteryBlockEntity extends BlockEntity implements IPhotonAcceptor {

    // Capacités par tier
    public static final long T1_CAPACITY =    50_000L;
    public static final long T2_CAPACITY =   250_000L;
    public static final long T3_CAPACITY = 1_000_000L;
    public static final long T4_CAPACITY = 4_000_000L;

    private static final long RECEIVE_RATE   = 5L;
    private static final long DISCHARGE_RATE = 20L;

    private final long maxPhotons;
    private long storedPhotons  = 0;
    private int  beamActiveTicks = 0;

    public LightBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIGHT_BATTERY.get(), pos, state);
        this.maxPhotons = capacityOf(state.getBlock());
    }

    /** Déduit la capacité du type de bloc. */
    public static long capacityOf(Block block) {
        if (block instanceof LightBatteryT4Block) return T4_CAPACITY;
        if (block instanceof LightBatteryT3Block) return T3_CAPACITY;
        if (block instanceof LightBatteryT2Block) return T2_CAPACITY;
        return T1_CAPACITY;
    }

    public void receiveBeam(float wavelength) {
        long space = maxPhotons - storedPhotons;
        if (space > 0) {
            storedPhotons += Math.min(RECEIVE_RATE, space);
            setChanged();
        }
        beamActiveTicks = 3;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  LightBatteryBlockEntity be) {
        boolean receiving = be.beamActiveTicks > 0;
        if (be.beamActiveTicks > 0) be.beamActiveTicks--;

        if (!receiving && be.storedPhotons > 0) {
            for (Direction dir : Direction.values()) {
                if (be.storedPhotons <= 0) break;
                BlockPos neighbor = pos.relative(dir);
                if (!(level.getBlockEntity(neighbor) instanceof IPhotonAcceptor acceptor)) continue;
                if (acceptor instanceof LightBatteryBlockEntity) continue; // pas batterie→batterie
                long space = acceptor.getMaxPhotons() - acceptor.getStoredPhotons();
                if (space <= 0) continue;
                long transfer = Math.min(be.storedPhotons, Math.min(space, DISCHARGE_RATE));
                be.storedPhotons -= transfer;
                acceptor.addPhotons(transfer);
                be.setChanged();
            }
        }

        boolean charged = be.storedPhotons > 0;
        if (state.getValue(LightBatteryBlock.CHARGED) != charged) {
            level.setBlock(pos, state.setValue(LightBatteryBlock.CHARGED, charged), 3);
        }
    }

    @Override
    public void addPhotons(long amount) {
        storedPhotons = Math.min(maxPhotons, storedPhotons + amount);
        setChanged();
    }

    @Override public long getStoredPhotons() { return storedPhotons; }
    @Override public long getMaxPhotons()    { return maxPhotons; }
    public boolean isReceiving()             { return beamActiveTicks > 0; }

    public ContainerData getContainerData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int)(storedPhotons & 0x7FFF);
                    case 1 -> (int)(storedPhotons >> 15);
                    case 2 -> (int)(maxPhotons & 0x7FFF);
                    case 3 -> (int)(maxPhotons >> 15);
                    case 4 -> beamActiveTicks > 0 ? 1 : 0;
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 5; }
        };
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (getLevel() != null)
            getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("photons", storedPhotons);
        output.putInt("beamTicks", beamActiveTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedPhotons   = input.getLongOr("photons", 0L);
        beamActiveTicks = input.getIntOr("beamTicks", 0);
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
