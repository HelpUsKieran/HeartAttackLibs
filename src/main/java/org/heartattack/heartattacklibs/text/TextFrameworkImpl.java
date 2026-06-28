package org.heartattack.heartattacklibs.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class TextFrameworkImpl implements TextFramework {
    private static final LegacyComponentSerializer LEGACY_SECTION = LegacyComponentSerializer.legacySection();
    private final MiniMessageService miniMessage;

    public TextFrameworkImpl(MiniMessageService miniMessage) {
        this.miniMessage = miniMessage;
    }

    @Override
    public MiniMessageService miniMessage() {
        return miniMessage;
    }

    @Override
    public void setPrefix(String miniMessagePrefix) {
        miniMessage.setPrefix(miniMessagePrefix);
    }

    @Override
    public Component deserialize(String message, TagResolver... resolvers) {
        return miniMessage.deserialize(message, resolvers);
    }

    @Override
    public void send(CommandSender sender, String message, TagResolver... resolvers) {
        miniMessage.send(sender, message, resolvers);
    }

    @Override
    public void sendWithPrefix(CommandSender sender, String message, TagResolver... resolvers) {
        miniMessage.sendWithPrefix(sender, message, resolvers);
    }

    @Override
    public void send(Player player, String message, TagResolver... resolvers) {
        miniMessage.send(player, message, resolvers);
    }

    @Override
    public Component color(String text) {
        return miniMessage.deserialize(text);
    }

    @Override
    public Component color(String text, Map<String, String> placeholders) {
        if (text == null) {
            return Component.empty();
        }
        String rendered = text;
        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                rendered = rendered.replace(entry.getKey(), entry.getValue() == null ? "" : entry.getValue());
            }
        }
        return miniMessage.deserialize(rendered);
    }

    @Override
    public String colorForLegacy(String text) {
        return LEGACY_SECTION.serialize(miniMessage.deserialize(text));
    }
}
