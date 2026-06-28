package dev.crococrystal.occeendercable.common.block;

import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ComponentDeviceBlock extends Block implements EntityBlock {
    private final BiFunction<BlockPos, BlockState, BlockEntity> factory;

    public ComponentDeviceBlock(Properties properties, BiFunction<BlockPos, BlockState, BlockEntity> factory) {
        super(properties);
        this.factory = factory;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
