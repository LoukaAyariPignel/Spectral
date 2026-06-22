package fr.skylined.spectral.block.custom;

import com.mojang.serialization.MapCodec;
import fr.skylined.spectral.block.entity.PrismStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PrismStandBlock extends BaseEntityBlock {

    public static final MapCodec<PrismStandBlock> CODEC = simpleCodec(PrismStandBlock::new);

    public PrismStandBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PrismStandBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!(level.getBlockEntity(pos) instanceof PrismStandBlockEntity be))
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!be.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (!stack.isEmpty()) {
            if (!level.isClientSide()) {
                be.setStoredItem(stack.copyWithCount(1));
                if (!player.isCreative()) stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PrismStandBlockEntity be)) return InteractionResult.PASS;

        if (!be.isEmpty()) {
            if (!level.isClientSide()) {
                player.getInventory().placeItemBackInInventory(be.getStoredItem().copy());
                be.setStoredItem(ItemStack.EMPTY);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        if (level.getBlockEntity(pos) instanceof PrismStandBlockEntity be && !be.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.getStoredItem());
        }
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
    }
}
