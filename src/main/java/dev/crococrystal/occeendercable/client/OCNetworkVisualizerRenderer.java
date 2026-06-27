package dev.crococrystal.occeendercable.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.crococrystal.occeendercable.EnderCableMod;
import dev.crococrystal.occeendercable.common.block.EnderCableBlock;
import dev.crococrystal.occeendercable.common.registry.ModBlocks;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import li.cil.oc.api.network.Environment;
import li.cil.oc.common.capabilities.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = EnderCableMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OCNetworkVisualizerRenderer {
    private static final int SCAN_RADIUS = 24;
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final float NODE_RED = 1.0F;
    private static final float NODE_GREEN = 0.94F;
    private static final float NODE_BLUE = 0.0F;
    private static final float CABLE_RED = 0.08F;
    private static final float CABLE_GREEN = 1.0F;
    private static final float CABLE_BLUE = 0.72F;

    private static ScanResult cachedScan = ScanResult.EMPTY;
    private static int ticksUntilScan;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || !isVisualizerHeld(player)) {
            cachedScan = ScanResult.EMPTY;
            ticksUntilScan = 0;
            return;
        }

        if (ticksUntilScan-- <= 0) {
            cachedScan = scan(level, player.blockPosition());
            ticksUntilScan = SCAN_INTERVAL_TICKS;
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES || cachedScan.nodes.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || !isVisualizerHeld(player)) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());

        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        for (Map.Entry<BlockPos, NodeKind> entry : cachedScan.nodes.entrySet()) {
            renderNodeBox(poseStack, lines, entry.getKey(), entry.getValue());
        }
        for (Edge edge : cachedScan.edges) {
            renderEdge(poseStack, lines, edge);
        }

        buffer.endBatch(RenderType.lines());
        poseStack.popPose();
    }

    private static ScanResult scan(ClientLevel level, BlockPos center) {
        Map<BlockPos, NodeKind> nodes = new HashMap<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        int minX = center.getX() - SCAN_RADIUS;
        int maxX = center.getX() + SCAN_RADIUS;
        int minY = Math.max(-64, center.getY() - SCAN_RADIUS);
        int maxY = Math.min(320, center.getY() + SCAN_RADIUS);
        int minZ = center.getZ() - SCAN_RADIUS;
        int maxZ = center.getZ() + SCAN_RADIUS;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    cursor.set(x, y, z);
                    if (!level.isLoaded(cursor)) {
                        continue;
                    }
                    BlockState state = level.getBlockState(cursor);
                    if (state.getBlock() instanceof EnderCableBlock) {
                        nodes.put(cursor.immutable(), NodeKind.CABLE);
                        continue;
                    }

                    BlockEntity blockEntity = level.getBlockEntity(cursor);
                    if (hasOpenComputersNode(blockEntity)) {
                        nodes.put(cursor.immutable(), NodeKind.DEVICE);
                    }
                }
            }
        }

        Set<Edge> edgeSet = new HashSet<>();
        for (BlockPos pos : nodes.keySet()) {
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.relative(direction);
                if (!nodes.containsKey(neighborPos)) {
                    continue;
                }
                if (canConnect(level, pos, direction) && canConnect(level, neighborPos, direction.getOpposite())) {
                    edgeSet.add(Edge.of(pos, neighborPos));
                }
            }
        }

        return new ScanResult(nodes, List.copyOf(edgeSet));
    }

    private static boolean hasOpenComputersNode(BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.isRemoved()) {
            return false;
        }

        for (Direction side : Direction.values()) {
            if (blockEntity.getCapability(Capabilities.SidedEnvironmentCapability, side)
                .map(host -> host.canConnect(side))
                .orElse(false)) {
                return true;
            }
            if (blockEntity.getCapability(Capabilities.EnvironmentCapability, side).isPresent()) {
                return true;
            }
        }
        return false;
    }

    private static boolean canConnect(BlockGetter level, BlockPos pos, Direction direction) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof EnderCableBlock) {
            return EnderCableBlock.isConnected(state, direction);
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.isRemoved()) {
            return false;
        }

        return blockEntity.getCapability(Capabilities.SidedEnvironmentCapability, direction)
            .map(host -> host.canConnect(direction))
            .orElseGet(() -> blockEntity.getCapability(Capabilities.EnvironmentCapability, direction)
                .map(host -> host.node() != null || blockEntity.getLevel() == null || blockEntity.getLevel().isClientSide)
                .orElse(false));
    }

    private static boolean isVisualizerHeld(LocalPlayer player) {
        return isVisualizer(player.getMainHandItem()) || isVisualizer(player.getOffhandItem());
    }

    private static boolean isVisualizer(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ModBlocks.NETWORK_VISUALIZER.get());
    }

    private static void renderNodeBox(PoseStack poseStack, VertexConsumer consumer, BlockPos pos, NodeKind kind) {
        float red = kind == NodeKind.CABLE ? CABLE_RED : NODE_RED;
        float green = kind == NodeKind.CABLE ? CABLE_GREEN : NODE_GREEN;
        float blue = kind == NodeKind.CABLE ? CABLE_BLUE : NODE_BLUE;
        double pad = kind == NodeKind.CABLE ? 0.31D : 0.08D;
        LevelRenderer.renderLineBox(
            poseStack,
            consumer,
            pos.getX() + pad,
            pos.getY() + pad,
            pos.getZ() + pad,
            pos.getX() + 1.0D - pad,
            pos.getY() + 1.0D - pad,
            pos.getZ() + 1.0D - pad,
            red,
            green,
            blue,
            0.95F
        );
    }

    private static void renderEdge(PoseStack poseStack, VertexConsumer consumer, Edge edge) {
        double x1 = edge.a.getX() + 0.5D;
        double y1 = edge.a.getY() + 0.5D;
        double z1 = edge.a.getZ() + 0.5D;
        double x2 = edge.b.getX() + 0.5D;
        double y2 = edge.b.getY() + 0.5D;
        double z2 = edge.b.getZ() + 0.5D;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= 0.0D) {
            return;
        }

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float nx = (float) (dx / length);
        float ny = (float) (dy / length);
        float nz = (float) (dz / length);

        consumer.vertex(matrix, (float) x1, (float) y1, (float) z1)
            .color(NODE_RED, NODE_GREEN, NODE_BLUE, 1.0F)
            .normal(normal, nx, ny, nz)
            .endVertex();
        consumer.vertex(matrix, (float) x2, (float) y2, (float) z2)
            .color(NODE_RED, NODE_GREEN, NODE_BLUE, 1.0F)
            .normal(normal, nx, ny, nz)
            .endVertex();
    }

    private OCNetworkVisualizerRenderer() {
    }

    private enum NodeKind {
        CABLE,
        DEVICE
    }

    private record Edge(BlockPos a, BlockPos b) {
        static Edge of(BlockPos first, BlockPos second) {
            return first.asLong() <= second.asLong() ? new Edge(first, second) : new Edge(second, first);
        }
    }

    private record ScanResult(Map<BlockPos, NodeKind> nodes, List<Edge> edges) {
        static final ScanResult EMPTY = new ScanResult(Map.of(), List.of());
    }
}
