package org.heartattack.heartattacklibs.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.heartattack.heartattacklibs.text.UnicodeSmallCaps;

final class MiniMessageItem {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MiniMessageItem() {
    }

    static Component text(String miniMessage) {
        return MINI_MESSAGE.deserialize(UnicodeSmallCaps.apply(miniMessage == null ? "" : miniMessage));
    }

    static Component guiText(String miniMessage) {
        return text(miniMessage).decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
