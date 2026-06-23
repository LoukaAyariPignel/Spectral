package fr.skylined.spectral.block.custom;

import com.mojang.serialization.MapCodec;
import fr.skylined.spectral.block.entity.ModBlockEntities;
import fr.skylined.spectral.block.entity.SolarCollectorBlockEntity;
import fr.skylined.spectral.screen.SolarCollectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class SolarCollectorBlock extends BaseEntityBlock {

    public static final MapCodec<SolarCollectorBlock> CODEC = simpleCodec(SolarCollectorBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
        box(1,  0,  1,  15, 3,  15),  // base 14x3x14
        box(3,  3,  3,  13, 5,  13),  // palier 10x2x10
        box(3,  5,  3,  13, 6,  13),  // plateau cristal
        box(1,  5,  1,  4,  12, 4),   // pilier NW
        box(12, 5,  1,  15, 12, 4),   // pilier NE
        box(1,  5,  12, 4,  12, 15),  // pilier SW
        box(12, 5,  12, 15, 12, 15)   // pilier SE
    );

    public SolarCollectorBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SolarCollectorBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            if (level.getBlockEntity(pos) instanceof SolarCollectorBlockEntity be) {
                sp.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> new SolarCollectorMenu(id, inv, be),
                        Component.translatable("block.spectral.solar_collector")
                ));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.SOLAR_COLLECTOR.get(), SolarCollectorBlockEntity::serverTick);
    }
}
