package dev.crococrystal.occeendercable.common.util;

import dev.crococrystal.occeendercable.common.blockentity.ChatBoxBlockEntity;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraftforge.event.ServerChatEvent;

public final class ModChatEvents {
    private static final Set<ChatBoxBlockEntity> CHAT_BOXES = Collections.newSetFromMap(new WeakHashMap<>());

    public static void register(ChatBoxBlockEntity chatBox) {
        CHAT_BOXES.add(chatBox);
    }

    public static void unregister(ChatBoxBlockEntity chatBox) {
        CHAT_BOXES.remove(chatBox);
    }

    public static void onServerChat(ServerChatEvent event) {
        for (ChatBoxBlockEntity chatBox : CHAT_BOXES.toArray(new ChatBoxBlockEntity[0])) {
            chatBox.receiveChat(event.getPlayer(), event.getRawText());
        }
    }

    private ModChatEvents() {
    }
}
