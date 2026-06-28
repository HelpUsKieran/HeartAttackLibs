package org.heartattack.heartattacklibs.dependency.vault;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.dependency.provider.ChatProvider;

public final class VaultChatProvider implements ChatProvider {
    private final Chat chat;

    public VaultChatProvider(Chat chat) {
        this.chat = chat;
    }

    @Override
    public boolean available() {
        return chat != null;
    }

    @Override
    public String prefix(Player player) {
        if (player == null) {
            return "";
        }
        return chat.getPlayerPrefix(player);
    }

    @Override
    public String suffix(Player player) {
        if (player == null) {
            return "";
        }
        return chat.getPlayerSuffix(player);
    }
}
