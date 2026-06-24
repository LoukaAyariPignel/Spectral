package fr.skylined.spectral.block.custom;

import com.mojang.serialization.MapCodec;
import fr.skylined.spectral.block.entity.LightEmitterBlockEntity;
import fr.skylined.spectral.block.entity.ModBlockEntities;
import fr.skylined.spectral.screen.LightEmitterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class LightEmitterBlock extends BaseEntityBlock {

    public static final MapCodec<LightEmitterBlock> CODEC = simpleCodec(LightEmitterBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Base + corps (symétriques pour toutes les directions)
    private static final VoxelShape BASE_BODY = Shapes.or(
        box(2, 0, 2, 14, 4, 14),    // base 12x4x12
        box(3, 4, 3, 13, 12, 13)    // corps 10x8x10
    );
    // Lentille qui dépasse selon la direction facing
    private static final VoxelShape SHAPE_S = Shapes.or(BASE_BODY, box(5, 5, 12, 11, 11, 16));
    private static final VoxelShape SHAPE_N = Shapes.or(BASE_BODY, box(5, 5,  0, 11, 11,  4));
    private static final VoxelShape SHAPE_E = Shapes.or(BASE_BODY, box(12, 5, 5, 16, 11, 11));
    private static final VoxelShape SHAPE_W = Shapes.or(BASE_BODY, box( 0, 5, 5,  4, 11, 11));

    public LightEmitterBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof LightEmitterBlockEntity be) {
            Direction facing = state.getValue(FACING);
            be.setDirection(facing.getStepX(), 0f, facing.getStepZ());
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_N;
            case EAST  -> SHAPE_E;
            case WEST  -> SHAPE_W;
            default    -> SHAPE_S;
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightEmitterBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            if (level.getBlockEntity(pos) instanceof LightEmitterBlockEntity be) {
                sp.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> new LightEmitterMenu(id, inv, be),
                        Component.translatable("block.spectral.light_emitter")
                ));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExp) {
        if (level.getBlockEntity(pos) instanceof LightEmitterBlockEntity be) {
            Containers.dropContents(level, pos, be.getGemContainer());
        }
        super.spawnAfterBreak(state, level, pos, tool, dropExp);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.LIGHT_EMITTER.get(), LightEmitterBlockEntity::serverTick);
    }
}
