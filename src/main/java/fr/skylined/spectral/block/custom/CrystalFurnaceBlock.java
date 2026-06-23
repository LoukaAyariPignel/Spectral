package fr.skylined.spectral.block.custom;

import com.mojang.serialization.MapCodec;
import fr.skylined.spectral.block.entity.CrystalFurnaceBlockEntity;
import fr.skylined.spectral.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class CrystalFurnaceBlock extends BaseEntityBlock {

    public static final MapCodec<CrystalFurnaceBlock> CODEC = simpleCodec(CrystalFurnaceBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
        box(1, 0, 1, 15, 3, 15),   // base 14x3x14
        box(2, 3, 2, 14, 16, 14)   // corps 12x13x12
    );
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public CrystalFurnaceBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(LIT, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalFurnaceBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.CRYSTAL_FURNACE.get(), CrystalFurnaceBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer sp) {
            if (level.getBlockEntity(pos) instanceof CrystalFurnaceBlockEntity be) {
                sp.openMenu(new net.minecraft.world.SimpleMenuProvider(
                        (id, inv, p) -> new fr.skylined.spectral.screen.CrystalFurnaceMenu(id, inv, be),
                        net.minecraft.network.chat.Component.translatable("block.spectral.crystal_furnace")
                ));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.world.item.ItemStack tool, boolean dropExp) {
        if (level.getBlockEntity(pos) instanceof CrystalFurnaceBlockEntity be) {
            Containers.dropContents(level, pos, be.getInventory());
        }
        super.spawnAfterBreak(state, level, pos, tool, dropExp);
    }
}
