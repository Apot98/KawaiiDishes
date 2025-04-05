package com.hakimen.kawaiidishes.blocks;

import com.hakimen.kawaiidishes.blocks.block_entities.DisplayCaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.core.jmx.Server;
import org.jetbrains.annotations.Nullable;

public class DisplayCaseBlock extends DirectionalBlock implements EntityBlock {
    public DisplayCaseBlock() {
        super(Block.Properties.of(Material.GLASS)
                .strength(1.0F, 6.0F)
                .noOcclusion()
                .isSuffocating((p_61036_, p_61037_, p_61038_) -> false)
                .isViewBlocking((p_61036_, p_61037_, p_61038_) -> false)
        );
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean skipRendering(BlockState p_53972_, BlockState p_53973_, Direction p_53974_) {
        return p_53973_.is(this) || super.skipRendering(p_53972_, p_53973_, p_53974_);
    }

    @Override
    public VoxelShape getVisualShape(BlockState p_60479_, BlockGetter p_60480_, BlockPos p_60481_, CollisionContext p_60482_) {
        return Shapes.empty();
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> properties) {
        properties.add(FACING);
    }

    @Override
    @Deprecated
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    @Deprecated
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @javax.annotation.Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext placement) {
        return defaultBlockState().setValue(FACING, placement.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @javax.annotation.Nullable LivingEntity placer, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            BlockEntity tileEntity = level.getBlockEntity(pos);
            if (tileEntity instanceof DisplayCaseBlockEntity) {
                ((DisplayCaseBlockEntity) tileEntity).setCustomName(stack.getHoverName());
            }
        }
    }


    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof DisplayCaseBlockEntity coffeeMachine) {
                for (int i = 0; i < coffeeMachine.getInventory().getSlots(); i++) {
                    popResource(pLevel, pPos, coffeeMachine.getInventory().getStackInSlot(i));
                }
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? null
                : (level, pos, state, blockEntity) -> ((DisplayCaseBlockEntity) blockEntity).tick(level, pos, state, (DisplayCaseBlockEntity) blockEntity);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new DisplayCaseBlockEntity(pPos, pState);
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            if (pHit.getDirection().equals(pState.getValue(DirectionalBlock.FACING).getOpposite())) {
                BlockEntity entity = pLevel.getBlockEntity(pPos);
                if (entity instanceof DisplayCaseBlockEntity) {
                    NetworkHooks.openGui(((ServerPlayer) pPlayer), (DisplayCaseBlockEntity) entity, pPos);
                    //pPlayer.openMenu((DisplayCaseBlockEntity) entity);
                } else {
                    throw new IllegalStateException("Our Container provider is missing!");
                }
                return InteractionResult.SUCCESS;
            }
            else return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }
}
