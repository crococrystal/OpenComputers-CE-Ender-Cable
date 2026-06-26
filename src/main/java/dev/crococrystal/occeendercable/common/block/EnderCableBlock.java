package dev.crococrystal.occeendercable.common.block;

import dev.crococrystal.occeendercable.common.blockentity.EnderCableBlockEntity;
import dev.crococrystal.occeendercable.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class EnderCableBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.box(0.25D, 0.25D, 0.25D, 0.75D, 0.75D, 0.75D);

    public EnderCableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block changedBlock, BlockPos changedPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, changedBlock, changedPos, movedByPiston);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof EnderCableBlockEntity cable) {
            cable.joinNetwork();
        }
    }
}
