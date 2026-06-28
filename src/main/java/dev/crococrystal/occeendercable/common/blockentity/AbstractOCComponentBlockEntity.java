package dev.crococrystal.occeendercable.common.blockentity;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.SidedEnvironment;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.common.capabilities.Capabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractOCComponentBlockEntity extends BlockEntity implements Environment, EnvironmentHost, SidedEnvironment {
    private static final String TAG_NODE = "oc_node";

    protected final Node node;
    private final LazyOptional<Environment> environmentCapability = LazyOptional.of(() -> this);
    private final LazyOptional<SidedEnvironment> sidedEnvironmentCapability = LazyOptional.of(() -> this);

    protected AbstractOCComponentBlockEntity(BlockEntityType<?> type, String componentName, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.node = Network.newNode(this, Visibility.Network)
            .withComponent(componentName)
            .create();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        joinNetwork();
    }

    public void joinNetwork() {
        if (level != null && !level.isClientSide && node != null) {
            Network.joinOrCreateNetwork(this);
        }
    }

    @Override
    public Node node() {
        return node;
    }

    @Override
    public Node sidedNode(Direction side) {
        return node;
    }

    @Override
    public boolean canConnect(Direction side) {
        return true;
    }

    @Override
    public void onConnect(Node node) {
    }

    @Override
    public void onDisconnect(Node node) {
    }

    @Override
    public void onMessage(Message message) {
    }

    @Override
    public Level getEnvironmentLevel() {
        return level;
    }

    @Override
    public double xPosition() {
        return worldPosition.getX() + 0.5D;
    }

    @Override
    public double yPosition() {
        return worldPosition.getY() + 0.5D;
    }

    @Override
    public double zPosition() {
        return worldPosition.getZ() + 0.5D;
    }

    @Override
    public void markChanged() {
        setChanged();
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (node != null && tag.contains(TAG_NODE)) {
            node.loadData(tag.getCompound(TAG_NODE));
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (node != null) {
            CompoundTag nodeTag = new CompoundTag();
            node.saveData(nodeTag);
            tag.put(TAG_NODE, nodeTag);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        removeNode();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        environmentCapability.invalidate();
        sidedEnvironmentCapability.invalidate();
        removeNode();
    }

    protected void removeNode() {
        if (node != null) {
            node.remove();
        }
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == Capabilities.EnvironmentCapability) {
            return environmentCapability.cast();
        }
        if (capability == Capabilities.SidedEnvironmentCapability) {
            return sidedEnvironmentCapability.cast();
        }
        return super.getCapability(capability, side);
    }
}
