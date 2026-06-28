package dev.crococrystal.occeendercable.common.block;

import dev.crococrystal.occeendercable.common.blockentity.ChatBoxBlockEntity;
import dev.crococrystal.occeendercable.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class ChatBoxBlock extends ComponentDeviceBlock {
    public ChatBoxBlock(Properties properties) {
        super(properties, ChatBoxBlockEntity::new);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return getSignal(state, level, pos, side);
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        if (level.getBlockEntity(pos) instanceof ChatBoxBlockEntity chatBox && chatBox.isPulsing()) {
            return 15;
        }
        return 0;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.CHAT_BOX.get()) {
            return (tickLevel, pos, tickState, blockEntity) -> {
                if (!tickLevel.isClientSide && blockEntity instanceof ChatBoxBlockEntity chatBox) {
                    chatBox.serverTick();
                }
            };
        }
        return null;
    }
}
