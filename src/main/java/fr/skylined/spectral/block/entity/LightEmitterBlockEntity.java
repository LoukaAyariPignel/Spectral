package fr.skylined.spectral.block.entity;

import fr.skylined.spectral.beam.BeamSegment;
import fr.skylined.spectral.block.custom.LightEmitterBlock;
import fr.skylined.spectral.component.ModComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import fr.skylined.spectral.client.color.WavelengthTintSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
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
    public static final int  BEAM_MAX_RANGE = 32;

    private long storedPhotons = 0;
    private boolean emitting = false;
    private List<BeamSegment> currentSegments = List.of();
    private final SimpleContainer gemContainer = new SimpleContainer(1);

    // Normalized 3D direction the beam fires in. Default: south (+Z).
    private float dirX = 0f, dirY = 0f, dirZ = 1f;

    // Sweep mode: rotates through all directions automatically (not persisted).
    private boolean sweeping = false;
    private int sweepTick = 0;

    public LightEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LIGHT_EMITTER.get(), pos, state);
    }

    /** Set beam direction from an arbitrary vector (will be normalized). */
    public void setDirection(float dx, float dy, float dz) {
        float len = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
        if (len < 1e-6f) return;
        dirX = dx / len; dirY = dy / len; dirZ = dz / len;
        setChanged();
    }

    public float getDirX() { return dirX; }
    public float getDirY() { return dirY; }
    public float getDirZ() { return dirZ; }

    public void setSweeping(boolean sweeping) { this.sweeping = sweeping; }
    public boolean isSweeping() { return sweeping; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LightEmitterBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        boolean wasEmitting = be.emitting;

        if (be.storedPhotons >= EMISSION_THRESHOLD) {
            be.storedPhotons -= EMISSION_COST;
            be.emitting = true;
        } else {
            be.emitting = false;
        }

        if (be.sweeping) {
            be.sweepTick++;
            // Spiral through the full sphere: theta rotates fast, phi oscillates slowly
            float theta = be.sweepTick * 0.05f;
            float phi   = (float) (Math.sin(be.sweepTick * 0.013) * Math.PI / 2.0);
            float cosPhi = (float) Math.cos(phi);
            be.dirX = cosPhi * (float) Math.sin(theta);
            be.dirY = (float) Math.sin(phi);
            be.dirZ = cosPhi * (float) Math.cos(theta);
            be.setChanged();
        }

        List<BeamSegment> newSegments;

        if (be.emitting) {
            float initialWavelength = 0f;
            ItemStack gem = be.gemContainer.getItem(0);
            if (!gem.isEmpty() && gem.has(ModComponents.WAVE_LENGTH.get())) {
                initialWavelength = gem.get(ModComponents.WAVE_LENGTH.get());
            }
            newSegments = computeBeam(serverLevel, pos, be.dirX, be.dirY, be.dirZ, initialWavelength);
            spawnBeamParticle(serverLevel, pos, be.dirX, be.dirY, be.dirZ, initialWavelength);
            deliverBeamToTerminal(serverLevel, pos, be.dirX, be.dirY, be.dirZ, newSegments);
        } else {
            newSegments = List.of();
        }

        boolean segmentsChanged = !newSegments.equals(be.currentSegments);
        be.currentSegments = newSegments;

        if (be.emitting != wasEmitting || segmentsChanged) {
            be.setChanged();
        }
    }

    /**
     * DDA (Amanatides & Woo) ray traversal. Handles arbitrary 3D directions including
     * diagonals (22.5°, 45°, etc.) by advancing all axes simultaneously when they tie.
     * Segment start/end values are distances from the emitter block center.
     */
    private static List<BeamSegment> computeBeam(ServerLevel level, BlockPos origin,
            float dirX, float dirY, float dirZ, float initialWavelength) {
        float len = (float) Math.sqrt(dirX*dirX + dirY*dirY + dirZ*dirZ);
        if (len < 1e-6f) return List.of();
        final float dx = dirX/len, dy = dirY/len, dz = dirZ/len;

        List<BeamSegment> segments = new ArrayList<>();
        float currentWavelength = initialWavelength;

        int stepX = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
        int stepY = dy > 0 ? 1 : (dy < 0 ? -1 : 0);
        int stepZ = dz > 0 ? 1 : (dz < 0 ? -1 : 0);

        // Ray distance to cross one voxel in each axis
        double tDX = stepX != 0 ? 1.0 / Math.abs(dx) : Double.MAX_VALUE;
        double tDY = stepY != 0 ? 1.0 / Math.abs(dy) : Double.MAX_VALUE;
        double tDZ = stepZ != 0 ? 1.0 / Math.abs(dz) : Double.MAX_VALUE;

        // Distance from block center to first boundary (= 0.5 / |d|)
        double tMaxX = stepX != 0 ? 0.5 * tDX : Double.MAX_VALUE;
        double tMaxY = stepY != 0 ? 0.5 * tDY : Double.MAX_VALUE;
        double tMaxZ = stepZ != 0 ? 0.5 * tDZ : Double.MAX_VALUE;

        int vx = origin.getX(), vy = origin.getY(), vz = origin.getZ();

        // Step out of origin block, advancing all tied axes simultaneously
        // so diagonal beams (45°, 22.5°…) don't get stuck at corners
        double tExit = Math.min(tMaxX, Math.min(tMaxY, tMaxZ));
        if (tMaxX <= tExit + 1e-9) { vx += stepX; tMaxX += tDX; }
        if (tMaxY <= tExit + 1e-9) { vy += stepY; tMaxY += tDY; }
        if (tMaxZ <= tExit + 1e-9) { vz += stepZ; tMaxZ += tDZ; }

        float segStart = (float) tExit; // beam is visible from the origin face
        float t = segStart;

        while (t < BEAM_MAX_RANGE) {
            BlockPos checkPos = new BlockPos(vx, vy, vz);
            if (!level.isLoaded(checkPos)) break;

            BlockState checkState = level.getBlockState(checkPos);

            if (!checkState.isAir()) {
                if (level.getBlockEntity(checkPos) instanceof PrismStandBlockEntity psbe) {
                    segments.add(new BeamSegment(segStart, t, currentWavelength));
                    ItemStack stored = psbe.getStoredItem();
                    if (!stored.isEmpty() && stored.has(ModComponents.WAVE_LENGTH.get())) {
                        currentWavelength = stored.get(ModComponents.WAVE_LENGTH.get());
                        segStart = t;
                    } else {
                        return segments;
                    }
                } else if (level.getBlockEntity(checkPos) instanceof CrystalFurnaceBlockEntity) {
                    segments.add(new BeamSegment(segStart, t, currentWavelength));
                    return segments;
                } else if (checkState.canOcclude()) {
                    segments.add(new BeamSegment(segStart, t, currentWavelength));
                    return segments;
                }
            }

            // Advance to next voxel, advancing all tied axes (handles diagonals)
            double tNext = Math.min(tMaxX, Math.min(tMaxY, tMaxZ));
            if (tMaxX <= tNext + 1e-9) { vx += stepX; tMaxX += tDX; }
            if (tMaxY <= tNext + 1e-9) { vy += stepY; tMaxY += tDY; }
            if (tMaxZ <= tNext + 1e-9) { vz += stepZ; tMaxZ += tDZ; }
            t = (float) Math.min(tNext, BEAM_MAX_RANGE);
        }

        if (segStart < BEAM_MAX_RANGE) {
            segments.add(new BeamSegment(segStart, BEAM_MAX_RANGE, currentWavelength));
        }
        return segments;
    }

    private static void deliverBeamToTerminal(ServerLevel level, BlockPos origin,
            float dx, float dy, float dz, List<BeamSegment> segments) {
        if (segments.isEmpty()) return;
        BeamSegment last = segments.get(segments.size() - 1);
        float endT = last.end();
        if (endT >= BEAM_MAX_RANGE - 0.01f) return;

        // Push slightly past the boundary to land inside the terminal block
        double eps = 0.02;
        BlockPos terminalPos = new BlockPos(
            (int) Math.floor(origin.getX() + 0.5 + dx * (endT + eps)),
            (int) Math.floor(origin.getY() + 0.5 + dy * (endT + eps)),
            (int) Math.floor(origin.getZ() + 0.5 + dz * (endT + eps))
        );

        if (level.getBlockEntity(terminalPos) instanceof CrystalFurnaceBlockEntity furnace) {
            furnace.receiveBeam(last.wavelength());
        }
    }

    private static void spawnBeamParticle(ServerLevel level, BlockPos pos,
            float dx, float dy, float dz, float wavelength) {
        double cx = pos.getX() + 0.5 + dx * 0.7;
        double cy = pos.getY() + 0.5 + dy * 0.7;
        double cz = pos.getZ() + 0.5 + dz * 0.7;

        if (wavelength >= 380f && wavelength <= 780f) {
            int argb = WavelengthTintSource.colorFromWavelength(wavelength);
            var dust = new DustParticleOptions(argb, 1.2f);
            level.sendParticles(dust, cx, cy, cz, 1, dx * 0.15, dy * 0.15, dz * 0.15, 0.01);
        } else {
            level.sendParticles(ParticleTypes.END_ROD, cx, cy, cz, 1, dx * 0.15, dy * 0.15, dz * 0.15, 0.01);
        }
    }

    public SimpleContainer getGemContainer()           { return gemContainer; }

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
        output.putFloat("dirX", dirX);
        output.putFloat("dirY", dirY);
        output.putFloat("dirZ", dirZ);
        if (!gemContainer.getItem(0).isEmpty())
            output.store("gem", ItemStack.CODEC, gemContainer.getItem(0));
        var segList = output.list("segs", BeamSegment.CODEC);
        for (BeamSegment seg : currentSegments) segList.add(seg);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedPhotons   = input.getLongOr("photons", 0L);
        emitting        = input.getBooleanOr("emitting", false);
        dirX            = input.getFloatOr("dirX", 0f);
        dirY            = input.getFloatOr("dirY", 0f);
        dirZ            = input.getFloatOr("dirZ", 1f);
        gemContainer.setItem(0, input.read("gem", ItemStack.CODEC).orElse(ItemStack.EMPTY));
        currentSegments = input.listOrEmpty("segs", BeamSegment.CODEC).stream().toList();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
