package dev.crococrystal.occeendercable.common.block;

import dev.crococrystal.occeendercable.common.blockentity.EnderCableBlockEntity;
import dev.crococrystal.occeendercable.common.registry.ModBlockEntities;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.SidedEnvironment;
import li.cil.oc.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

public final class EnderCableBlock extends Block implements EntityBlock {
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");

    private static final double CORE_MIN = 4.75D / 16.0D;
    private static final double CORE_MAX = 11.25D / 16.0D;
    private static final double ARM_MIN = 6.5D / 16.0D;
    private static final double ARM_MAX = 9.5D / 16.0D;

    private static final VoxelShape CORE_SHAPE = Shapes.box(CORE_MIN, CORE_MIN, CORE_MIN, CORE_MAX, CORE_MAX, CORE_MAX);
    private static final VoxelShape DOWN_SHAPE = Shapes.box(ARM_MIN, 0.0D, ARM_MIN, ARM_MAX, CORE_MIN, ARM_MAX);
    private static final VoxelShape UP_SHAPE = Shapes.box(ARM_MIN, CORE_MAX, ARM_MIN, ARM_MAX, 1.0D, ARM_MAX);
    private static final VoxelShape NORTH_SHAPE = Shapes.box(ARM_MIN, ARM_MIN, 0.0D, ARM_MAX, ARM_MAX, CORE_MIN);
    private static final VoxelShape SOUTH_SHAPE = Shapes.box(ARM_MIN, ARM_MIN, CORE_MAX, ARM_MAX, ARM_MAX, 1.0D);
    private static final VoxelShape WEST_SHAPE = Shapes.box(0.0D, ARM_MIN, ARM_MIN, CORE_MIN, ARM_MAX, ARM_MAX);
    private static final VoxelShape EAST_SHAPE = Shapes.box(CORE_MAX, ARM_MIN, ARM_MIN, 1.0D, ARM_MAX, ARM_MAX);
    private static final VoxelShape[] SHAPES = buildShapeCache();

    public EnderCableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(DOWN, false)
            .setValue(UP, false)
            .setValue(NORTH, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(EAST, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[connectionMask(state)];
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.ENDER_CABLE.get().create(pos, state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return updateConnections(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DOWN, UP, NORTH, SOUTH, WEST, EAST);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            joinNetwork(level, pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(propertyFor(direction), canConnectTo(level, neighborPos, direction));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, changedBlock, changedPos, movedByPiston);
        if (!level.isClientSide) {
            BlockState updated = updateConnections(state, level, pos);
            if (updated != state) {
                level.setBlock(pos, updated, Block.UPDATE_CLIENTS | Block.UPDATE_NEIGHBORS | Block.UPDATE_KNOWN_SHAPE);
            }
            joinNetwork(level, pos);
        }
    }

    public static boolean isConnected(BlockState state, Direction direction) {
        return state.getValue(propertyFor(direction));
    }

    private static void joinNetwork(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof EnderCableBlockEntity cable) {
            cable.joinNetwork();
        }
    }

    private static BlockState updateConnections(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState updated = state;
        for (Direction direction : Direction.values()) {
            updated = updated.setValue(propertyFor(direction), canConnectTo(level, pos.relative(direction), direction));
        }
        return updated;
    }

    private static boolean canConnectTo(BlockGetter level, BlockPos neighborPos, Direction directionToNeighbor) {
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof EnderCableBlock) {
            return true;
        }

        BlockEntity neighbor = level.getBlockEntity(neighborPos);
        if (neighbor == null || neighbor.isRemoved() || neighbor.getLevel() == null) {
            return false;
        }

        Direction sideOnNeighbor = directionToNeighbor.getOpposite();
        LazyOptional<SidedEnvironment> sidedEnvironment = neighbor.getCapability(Capabilities.SidedEnvironmentCapability, sideOnNeighbor);
        if (sidedEnvironment.isPresent()) {
            SidedEnvironment host = sidedEnvironment.orElse(null);
            if (host == null || !host.canConnect(sideOnNeighbor)) {
                return false;
            }
            return neighbor.getLevel().isClientSide || host.sidedNode(sideOnNeighbor) != null;
        }

        LazyOptional<Environment> environment = neighbor.getCapability(Capabilities.EnvironmentCapability, sideOnNeighbor);
        if (environment.isPresent()) {
            Environment host = environment.orElse(null);
            return host != null && (neighbor.getLevel().isClientSide || host.node() != null);
        }

        return false;
    }

    private static BooleanProperty propertyFor(Direction direction) {
        return switch (direction) {
            case DOWN -> DOWN;
            case UP -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }

    private static int connectionMask(BlockState state) {
        int mask = 0;
        for (Direction direction : Direction.values()) {
            if (isConnected(state, direction)) {
                mask |= 1 << direction.get3DDataValue();
            }
        }
        return mask;
    }

    private static VoxelShape[] buildShapeCache() {
        VoxelShape[] shapes = new VoxelShape[64];
        for (int mask = 0; mask < shapes.length; mask++) {
            VoxelShape shape = CORE_SHAPE;
            if ((mask & (1 << Direction.DOWN.get3DDataValue())) != 0) {
                shape = Shapes.or(shape, DOWN_SHAPE);
            }
            if ((mask & (1 << Direction.UP.get3DDataValue())) != 0) {
                shape = Shapes.or(shape, UP_SHAPE);
            }
            if ((mask & (1 << Direction.NORTH.get3DDataValue())) != 0) {
                shape = Shapes.or(shape, NORTH_SHAPE);
            }
            if ((mask & (1 << Direction.SOUTH.get3DDataValue())) != 0) {
                shape = Shapes.or(shape, SOUTH_SHAPE);
            }
            if ((mask & (1 << Direction.WEST.get3DDataValue())) != 0) {
                shape = Shapes.or(shape, WEST_SHAPE);
            }
            if ((mask & (1 << Direction.EAST.get3DDataValue())) != 0) {
                shape = Shapes.or(shape, EAST_SHAPE);
            }
            shapes[mask] = shape.optimize();
        }
        return shapes;
    }
}
