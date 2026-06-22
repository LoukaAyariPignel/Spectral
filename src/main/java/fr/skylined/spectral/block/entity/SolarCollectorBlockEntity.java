package fr.skylined.spectral.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SolarCollectorBlockEntity extends BlockEntity {

    public static final long MAX_PHOTONS = 5000L;
    private static final int BASE_PRODUCTION = 10;

    private long storedPhotons = 0;
    private int currentProduction = 0;

    public SolarCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOLAR_COLLECTOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SolarCollectorBlockEntity be) {
        int produced = calculateProduction(level, pos);
        be.currentProduction = produced;

        if (produced > 0 && be.storedPhotons < MAX_PHOTONS) {
            be.storedPhotons = Math.min(MAX_PHOTONS, be.storedPhotons + produced);
            be.setChanged();
        }

        pushToAdjacentEmitters(level, pos, be);
    }

    private static int calculateProduction(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        if (!level.canSeeSky(above)) return 0;
        int skyLight = level.getBrightness(LightLayer.SKY, above);
        if (skyLight <= 3) return 0;
        int base = Math.round(skyLight / 15f * BASE_PRODUCTION);
        return level.isRaining() ? Math.min(2, base) : base;
    }

    private static void pushToAdjacentEmitters(Level level, BlockPos pos, SolarCollectorBlockEntity be) {
        if (be.storedPhotons <= 0) return;
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) continue;
            BlockPos neighbor = pos.relative(dir);
            if (!(level.getBlockEntity(neighbor) instanceof LightEmitterBlockEntity emitter)) continue;
            long space = LightEmitterBlockEntity.MAX_PHOTONS - emitter.getStoredPhotons();
            if (space <= 0) continue;
            long transfer = Math.min(be.storedPhotons, Math.min(space, 20L));
            be.storedPhotons -= transfer;
            emitter.addPhotons(transfer);
            be.setChanged();
        }
    }

    public long getStoredPhotons() { return storedPhotons; }
    public int getCurrentProduction() { return currentProduction; }

    public ContainerData getContainerData() {
        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> (int) storedPhotons;
                    case 1 -> (int) MAX_PHOTONS;
                    case 2 -> currentProduction;
                    default -> 0;
                };
            }
            @Override public void set(int index, int value) {}
            @Override public int getCount() { return 3; }
        };
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("photons", storedPhotons);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedPhotons = input.getLongOr("photons", 0L);
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
