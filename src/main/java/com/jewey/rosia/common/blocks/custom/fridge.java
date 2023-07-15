package com.jewey.rosia.common.blocks.custom;

import com.jewey.rosia.common.blocks.MultiblockDevice;
import com.jewey.rosia.common.blocks.entity.ModBlockEntities;
import com.jewey.rosia.common.blocks.entity.block_entity.FridgeBlockEntity;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;


public class fridge extends MultiblockDevice
{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty ON = BooleanProperty.create("on");

    public fridge(ExtendedProperties properties)
    {
        super(properties, InventoryRemoveBehavior.DROP);
        registerDefaultState(getStateDefinition().any()
                .setValue(ON, false)
                .setValue(DUMMY, false));
    }

    @Override
    public Supplier<BlockItem> blockItemSupplier(CreativeModeTab group) {
        return () -> new FridgeBlockItem(this, group);
    }

    @Override
    public Direction getDummyOffsetDir() {
        return Direction.UP;
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(ON, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        FridgeBlockEntity fridge = ModBlockEntities.FRIDGE_BLOCK_ENTITY.get().create(pos, state);
        assert fridge != null;
        fridge.isDummy = state.getValue(DUMMY);
        return fridge;
    }

    // Voxel Shape
    private static final VoxelShape SHAPE = Block.box(0,0,0,16,16,16);

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type)
    {
        return false;
    }

    public static class FridgeBlockItem extends BlockItem
    {
        public FridgeBlockItem(Block block, CreativeModeTab group)
        {
            super(block, new Item.Properties().tab(group));
        }

        @Override
        protected boolean canPlace(BlockPlaceContext context, BlockState state)
        {
            if (super.canPlace(context, state))
            {
                BlockPos otherPos = context.getClickedPos().relative(Direction.UP);
                BlockState otherState = context.getLevel().getBlockState(otherPos);

                return otherState.isAir();
            }
            return false;
        }
    }
}