package dev.crococrystal.occeendercable.common.blockentity;

import dev.crococrystal.occeendercable.common.registry.ModBlockEntities;
import dev.crococrystal.occeendercable.common.util.ModChatEvents;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public final class ChatBoxBlockEntity extends AbstractOCComponentBlockEntity {
    private static final String TAG_DISTANCE = "distance";
    private static final String TAG_NAME = "name";
    private static final int DEFAULT_DISTANCE = 64;
    private static final int MAX_DISTANCE = 32767;

    private int distance = DEFAULT_DISTANCE;
    private String name = "Chat Box";
    private int pulseTicks;

    public ChatBoxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHAT_BOX.get(), "chat_box", pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            ModChatEvents.register(this);
        }
    }

    public void serverTick() {
        if (pulseTicks > 0) {
            pulseTicks--;
            if (pulseTicks == 0 && level != null) {
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            }
        }
    }

    public boolean isPulsing() {
        return pulseTicks > 0;
    }

    public void receiveChat(ServerPlayer player, String message) {
        if (level == null || player.level() != level) {
            return;
        }
        if (player.distanceToSqr(xPosition(), yPosition(), zPosition()) > (double) distance * distance) {
            return;
        }
        if (node != null) {
            node.sendToReachable("computer.signal", "chat_message", player.getGameProfile().getName(), message);
        }
        pulseTicks = 5;
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    @Callback(doc = "function(text:string[, distance:number]):boolean -- Makes the chat box say text nearby.")
    public Object[] say(Context context, Arguments arguments) {
        String text = arguments.checkString(0);
        int range = arguments.count() > 1 ? clamp(arguments.checkInteger(1), 1, MAX_DISTANCE) : distance;
        sendMessage(null, text, range);
        return new Object[] { true };
    }

    @Callback(doc = "function(player:string, text:string):boolean -- Sends a private system message to one player.")
    public Object[] tell(Context context, Arguments arguments) {
        String target = arguments.checkString(0);
        String text = arguments.checkString(1);
        sendMessage(target, text, MAX_DISTANCE);
        return new Object[] { true };
    }

    @Callback(direct = true, doc = "function():number -- Returns the chat listening/sending distance.")
    public Object[] getDistance(Context context, Arguments arguments) {
        return new Object[] { distance };
    }

    @Callback(direct = true, doc = "function(distance:number):number -- Sets the chat listening/sending distance.")
    public Object[] setDistance(Context context, Arguments arguments) {
        distance = clamp(arguments.checkInteger(0), 1, MAX_DISTANCE);
        setChanged();
        return new Object[] { distance };
    }

    @Callback(direct = true, doc = "function():string -- Returns the chat box name/prefix.")
    public Object[] getName(Context context, Arguments arguments) {
        return new Object[] { name };
    }

    @Callback(direct = true, doc = "function(name:string):string -- Sets the chat box name/prefix.")
    public Object[] setName(Context context, Arguments arguments) {
        name = arguments.checkString(0);
        setChanged();
        return new Object[] { name };
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_DISTANCE)) {
            distance = clamp(tag.getInt(TAG_DISTANCE), 1, MAX_DISTANCE);
        }
        if (tag.contains(TAG_NAME)) {
            name = tag.getString(TAG_NAME);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_DISTANCE, distance);
        tag.putString(TAG_NAME, name);
    }

    @Override
    public void onChunkUnloaded() {
        if (level != null && !level.isClientSide) {
            ModChatEvents.unregister(this);
        }
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            ModChatEvents.unregister(this);
        }
        super.setRemoved();
    }

    private void sendMessage(String targetName, String text, int range) {
        if (level == null || level.isClientSide || level.getServer() == null) {
            return;
        }

        Component message = Component.literal("[" + name + "] ")
            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            .append(Component.literal(text).withStyle(ChatFormatting.GRAY));

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (targetName != null && !player.getGameProfile().getName().equalsIgnoreCase(targetName)) {
                continue;
            }
            if (targetName == null && (player.level() != level || player.distanceToSqr(xPosition(), yPosition(), zPosition()) > (double) range * range)) {
                continue;
            }
            player.sendSystemMessage(message);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
