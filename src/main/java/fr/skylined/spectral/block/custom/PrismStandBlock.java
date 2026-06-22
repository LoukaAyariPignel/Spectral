package fr.skylined.spectral.block.custom;

import fr.skylined.spectral.block.entity.PrismStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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

    public PrismStandBlock(BlockBehaviour.Properties props) {
        super(props);
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof PrismStandBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!be.isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (!stack.isEmpty()) {
            if (!level.isClientSide) {
                be.setStoredItem(stack.copyWithCount(1));
                if (!player.isCreative()) stack.shrink(1);
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PrismStandBlockEntity be)) return InteractionResult.PASS;

        if (!be.isEmpty()) {
            if (!level.isClientSide) {
                player.getInventory().placeItemBackInInventory(be.getStoredItem().copy());
                be.setStoredItem(ItemStack.EMPTY);
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof PrismStandBlockEntity be && !be.isEmpty()) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), be.getStoredItem());
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
