package org.heartattack.heartattacklibs.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public interface TextFramework {
    MiniMessageService miniMessage();

    void setPrefix(String miniMessagePrefix);

    Component deserialize(String message, TagResolver... resolvers);

    void send(CommandSender sender, String message, TagResolver... resolvers);

    void sendWithPrefix(CommandSender sender, String message, TagResolver... resolvers);

    void send(Player player, String message, TagResolver... resolvers);

    Component color(String text);

    Component color(String text, Map<String, String> placeholders);

    String colorForLegacy(String text);
}
