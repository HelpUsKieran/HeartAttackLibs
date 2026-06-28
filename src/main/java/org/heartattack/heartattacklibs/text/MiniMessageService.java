package org.heartattack.heartattacklibs.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class MiniMessageService {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacyAmpersand = LegacyComponentSerializer.legacyAmpersand();
    private Component prefix = Component.empty();

    public void setPrefix(String miniMessagePrefix) {
        this.prefix = deserialize(miniMessagePrefix);
    }

    public Component deserialize(String message) {
        return deserialize(message, new TagResolver[0]);
    }

    public Component deserialize(String message, TagResolver... resolvers) {
        String source = message == null ? "" : message;
        TagResolver resolver = TagResolver.resolver(resolvers);
        Component parsed;

        if (containsMiniMessageTag(source)) {
            try {
                parsed = miniMessage.deserialize(source, resolver);
            } catch (Exception ignored) {
                parsed = legacyAmpersand.deserialize(source);
            }
        } else {
            parsed = legacyAmpersand.deserialize(source);
        }

        // Apply the small-caps font directly on the component tree. Serializing
        // to legacy text (the previous approach) destroyed click/hover/insertion
        // events, so links in announcements were silently dropped.
        return applyDefaultStyle(applySmallCaps(parsed));
    }

    /**
     * Recursively applies the small-caps transform to the text content of every
     * {@link TextComponent} in the tree, preserving styles, children, and
     * click/hover events (which live outside {@code children()}).
     */
    private Component applySmallCaps(Component component) {
        Component result = component;
        if (component instanceof TextComponent text) {
            result = text.content(UnicodeSmallCaps.apply(text.content()));
        }
        List<Component> children = component.children();
        if (!children.isEmpty()) {
            List<Component> transformed = new ArrayList<>(children.size());
            for (Component child : children) {
                transformed.add(applySmallCaps(child));
            }
            result = result.children(transformed);
        }
        return result;
    }

    public void send(CommandSender sender, String message, TagResolver... resolvers) {
        sender.sendMessage(deserialize(message, resolvers));
    }

    public void sendWithPrefix(CommandSender sender, String message, TagResolver... resolvers) {
        sender.sendMessage(prefix.append(deserialize(message, resolvers)));
    }

    public Component buildWithPrefix(String message, TagResolver... resolvers) {
        return prefix.append(deserialize(message, resolvers));
    }

    public void send(Player player, String message, TagResolver... resolvers) {
        player.sendMessage(deserialize(message, resolvers));
    }

    public TagResolver placeholder(String key, String value) {
        return Placeholder.parsed(key, value);
    }

    private boolean containsMiniMessageTag(String input) {
        return input.contains("<") && input.contains(">");
    }

    private Component applyDefaultStyle(Component component) {
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
