package fr.skylined.spectral.block.custom;

import com.mojang.serialization.MapCodec;
import fr.skylined.spectral.block.entity.LightBatteryBlockEntity;
import fr.skylined.spectral.block.entity.ModBlockEntities;
import fr.skylined.spectral.component.ModComponents;
import fr.skylined.spectral.screen.LightBatteryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class LightBatteryBlock extends BaseEntityBlock {

    public static final MapCodec<LightBatteryBlock> CODEC = simpleCodec(LightBatteryBlock::new);

    public static final BooleanProperty CHARGED = BooleanProperty.create("charged");

    public LightBatteryBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(stateDefinition.any().setValue(CHARGED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightBatteryBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** Injecte les photons stockés dans le DataComponent STORED_PHOTONS de l'item droppé. */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof LightBatteryBlockEntity battery) {
            long photons = battery.getStoredPhotons();
            if (photons > 0) {
                for (ItemStack stack : drops) {
                    if (stack.is(this.asItem())) {
                        stack.set(ModComponents.STORED_PHOTONS.get(), photons);
                    }
                }
            }
        }
        return drops;
    }

    /** Restaure les photons depuis le DataComponent STORED_PHOTONS lors de la pose. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (stack.has(ModComponents.STORED_PHOTONS.get())
                && level.getBlockEntity(pos) instanceof LightBatteryBlockEntity be) {
            be.addPhotons(stack.get(ModComponents.STORED_PHOTONS.get()));
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            if (level.getBlockEntity(pos) instanceof LightBatteryBlockEntity be) {
                sp.openMenu(new SimpleMenuProvider(
                        (id, inv, p) -> new LightBatteryMenu(id, inv, be),
                        Component.translatable("block.spectral.light_battery")
                ));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.LIGHT_BATTERY.get(),
                LightBatteryBlockEntity::serverTick);
    }
}
