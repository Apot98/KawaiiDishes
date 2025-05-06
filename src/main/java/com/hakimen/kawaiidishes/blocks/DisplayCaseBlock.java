package com.hakimen.kawaiidishes.blocks;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
//import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class DisplayCaseBlock extends HorizontalDirectionalBlock implements EntityBlock {

    @SuppressWarnings("UnstableApiUsage")
    public static final ImmutableMap<Direction, VoxelShape> COLLISION_SHAPE_FACING =
            Maps.immutableEnumMap(ImmutableMap.<Direction, VoxelShape>builder()
                    .put(Direction.NORTH, box(0.0D, 0.0D, 1.0D, 16.0D, 16.0D, 15.0D))
                    .put(Direction.SOUTH, box(0.0D, 0.0D, 1.0D, 16.0D, 16.0D, 15.0D))
                    .put(Direction.WEST, box(1.0D, 0.0D, 0.0D, 15.0D, 16.0D, 16.0D))
                    .put(Direction.EAST, box(1.0D, 0.0D, 0.0D, 15.0D, 16.0D, 16.0D))
                    .build());

    public DisplayCaseBlock() {
        super(Block.Properties.of(Material.GLASS)
                .sound(SoundType.GLASS)
                .strength(1.0F, 6.0F)
                .noOcclusion()
                .isSuffocating((p_61036_, p_61037_, p_61038_) -> false)
                .isViewBlocking((p_61036_, p_61037_, p_61038_) -> false)
        );
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext ctx) {
        return COLLISION_SHAPE_FACING.get(state.getValue(FACING));
    }

    @Override
    @Deprecated
    public boolean skipRendering(@Nonnull BlockState state, @Nonnull BlockState adjacentBlockState, @Nonnull Direction side) {
        return (side != state.getValue(FACING).getOpposite() && adjacentBlockState.getBlock() instanceof DisplayCaseBlock && adjacentBlockState.getValue(FACING) == state.getValue(FACING)) || super.skipRendering(state, adjacentBlockState, side);
    }

    @Override
    @Deprecated
    public float getShadeBrightness(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos) {
        return 1.0F;
    }
/*
    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }
*/
    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
        return true;
    }
/*
    @Nonnull
    @Override
    @Deprecated
    public VoxelShape getVisualShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    @Deprecated
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
        return box(1.0D, 1.0D, 1.0D, 15.0D, 15.0D, 15.0D);
    }
*/
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
            if (pHit.getDirection().equals(pState.getValue(HorizontalDirectionalBlock.FACING).getOpposite())) {
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
