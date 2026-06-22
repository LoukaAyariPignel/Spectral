package fr.skylined.spectral.block.entity;

import fr.skylined.spectral.beam.BeamSegment;
import fr.skylined.spectral.block.custom.LightEmitterBlock;
import fr.skylined.spectral.component.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class LightEmitterBlockEntity extends BlockEntity {

    public static final long MAX_PHOTONS = 1000L;
    private static final long EMISSION_THRESHOLD = 5L;
    private static final long EMISSION_COST = 5L;
    private static final int  BEAM_MAX_RANGE = 32;

    private long storedPhotons = 0;
    private boolean emitting = false;
    private List<BeamSegment> currentSegments = List.of();

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

        Direction facing = state.getValue(LightEmitterBlock.FACING);
        List<BeamSegment> newSegments;

        if (be.emitting) {
            newSegments = computeBeam(serverLevel, pos, facing);
            spawnBeamParticle(serverLevel, pos, facing);
            // Deliver beam energy to terminal machine
            deliverBeamToTerminal(serverLevel, pos, facing, newSegments);
        } else {
            newSegments = List.of();
        }

        boolean segmentsChanged = !newSegments.equals(be.currentSegments);
        be.currentSegments = newSegments;

        if (be.emitting != wasEmitting || segmentsChanged) {
            be.setChanged();
        }
    }

    private static List<BeamSegment> computeBeam(ServerLevel level, BlockPos origin, Direction facing) {
        List<BeamSegment> segments = new ArrayList<>();
        float currentWavelength = 0f;
        float segStart = 0f;

        for (int d = 1; d <= BEAM_MAX_RANGE; d++) {
            BlockPos checkPos = origin.relative(facing, d);
            if (!level.isLoaded(checkPos)) break;

            BlockState checkState = level.getBlockState(checkPos);

            if (checkState.isAir()) continue;

            if (level.getBlockEntity(checkPos) instanceof PrismStandBlockEntity psbe) {
                segments.add(new BeamSegment(segStart, d, currentWavelength));
                ItemStack stored = psbe.getStoredItem();
                if (!stored.isEmpty() && stored.has(ModComponents.WAVE_LENGTH.get())) {
                    currentWavelength = stored.get(ModComponents.WAVE_LENGTH.get());
                    segStart = d;
                } else {
                    return segments;
                }
                continue;
            }

            if (!checkState.canOcclude()) continue;

            segments.add(new BeamSegment(segStart, d, currentWavelength));
            return segments;
        }

        if (segStart < BEAM_MAX_RANGE) {
            segments.add(new BeamSegment(segStart, BEAM_MAX_RANGE, currentWavelength));
        }
        return segments;
    }

    private static void deliverBeamToTerminal(ServerLevel level, BlockPos origin, Direction facing,
            List<BeamSegment> segments) {
        if (segments.isEmpty()) return;
        BeamSegment last = segments.get(segments.size() - 1);
        int endDist = (int) last.end();
        if (endDist >= BEAM_MAX_RANGE) return; // beam went full range, no terminal block

        BlockPos terminalPos = origin.relative(facing, endDist);
        float wavelength = last.wavelength();

        if (level.getBlockEntity(terminalPos) instanceof CrystalFurnaceBlockEntity furnace) {
            furnace.receiveBeam(wavelength);
        }
        // Future machines can be added here
    }

    private static void spawnBeamParticle(ServerLevel level, BlockPos pos, Direction facing) {
        double cx = pos.getX() + 0.5 + facing.getStepX() * 0.7;
        double cy = pos.getY() + 0.5 + facing.getStepY() * 0.7;
        double cz = pos.getZ() + 0.5 + facing.getStepZ() * 0.7;
        level.sendParticles(ParticleTypes.END_ROD, cx, cy, cz, 1,
                facing.getStepX() * 0.15, facing.getStepY() * 0.15, facing.getStepZ() * 0.15, 0.01);
    }

    public void addPhotons(long amount) {
        storedPhotons = Math.min(MAX_PHOTONS, storedPhotons + amount);
        setChanged();
    }

    public long getStoredPhotons()                { return storedPhotons; }
    public boolean isEmitting()                   { return emitting; }
    public List<BeamSegment> getCurrentSegments() { return currentSegments; }

    public ContainerData getContainerData() {
        return new ContainerData() {
            @Override public int get(int index) {
                return switch (index) {
                    case 0 -> (int) storedPhotons;
                    case 1 -> (int) MAX_PHOTONS;
                    case 2 -> emitting ? 1 : 0;
                    case 3 -> {
                        Level lv = getLevel();
                        if (lv != null) {
                            BlockState bs = lv.getBlockState(getBlockPos());
                            if (bs.hasProperty(LightEmitterBlock.FACING))
                                yield bs.getValue(LightEmitterBlock.FACING).get2DDataValue();
                        }
                        yield 2;
                    }
                    default -> 0;
                };
            }
            @Override public void set(int i, int v) {}
            @Override public int getCount() { return 4; }
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
        output.putLong("photons", storedPhotons);
        output.putBoolean("emitting", emitting);
        var segList = output.list("segs", BeamSegment.CODEC);
        for (BeamSegment seg : currentSegments) segList.add(seg);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedPhotons   = input.getLongOr("photons", 0L);
        emitting        = input.getBooleanOr("emitting", false);
        currentSegments = input.listOrEmpty("segs", BeamSegment.CODEC).stream().toList();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
