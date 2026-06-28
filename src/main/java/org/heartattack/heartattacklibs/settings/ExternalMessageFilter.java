package org.heartattack.heartattacklibs.settings;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Intercepts outbound {@code ClientboundSystemChatPacket} and
 * {@code ClientboundSetActionBarTextPacket} packets by injecting a Netty
 * {@link ChannelDuplexHandler} directly into each player's pipeline.
 *
 * <p>This approach works without any special Paper API events and is robust
 * across all Paper 1.21 builds.  All NMS types are resolved via reflection
 * at runtime so there is no compile-time dependency on server internals.
 *
 * <p>Filter rules are loaded from {@code filter:} blocks in the per-plugin
 * settings YAML files and swapped atomically on each reload via
 * {@link #reload(Map)}.
 */
public final class ExternalMessageFilter implements Listener {

    private static final String HANDLER_NAME = "HeartAttackLibs_external_filter";

    // -----------------------------------------------------------------------
    // Packet reflection — resolved once at class-load time
    // -----------------------------------------------------------------------
    private static final Class<?> SYSTEM_CHAT_CLASS;
    /** ClientboundSystemChatPacket.content() → net.minecraft.network.chat.Component */
    private static final Method   CONTENT_METHOD;
    /** ClientboundSystemChatPacket.overlay() → boolean (true = actionbar overlay) */
    private static final Method   OVERLAY_METHOD;

    private static final Class<?> ACTION_BAR_CLASS;
    /** ClientboundSetActionBarTextPacket.text() → net.minecraft.network.chat.Component */
    private static final Method   TEXT_METHOD;

    /** PaperAdventure.asAdventure(NMS Component) → Adventure Component */
    private static final Method   AS_ADVENTURE_METHOD;

    static {
        SYSTEM_CHAT_CLASS  = tryClass("net.minecraft.network.protocol.game.ClientboundSystemChatPacket");
        CONTENT_METHOD     = tryMethod(SYSTEM_CHAT_CLASS, "content");
        OVERLAY_METHOD     = tryMethod(SYSTEM_CHAT_CLASS, "overlay");

        ACTION_BAR_CLASS   = tryClass("net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket");
        TEXT_METHOD        = tryMethod(ACTION_BAR_CLASS, "text");

        Class<?> paperAdventure = tryClass("io.papermc.paper.adventure.PaperAdventure");
        Class<?> nmsComponent   = tryClass("net.minecraft.network.chat.Component");
        Method asAdv = null;
        if (paperAdventure != null && nmsComponent != null) {
            try { asAdv = paperAdventure.getMethod("asAdventure", nmsComponent); }
            catch (NoSuchMethodException ignored) {}
        }
        AS_ADVENTURE_METHOD = asAdv;
    }

    // -----------------------------------------------------------------------
    // Channel reflection — resolved lazily on first player login
    // -----------------------------------------------------------------------
    /** CraftPlayer → ServerPlayer */
    private volatile Method getHandleMethod;
    /** ServerPlayer.connection → ServerGamePacketListenerImpl */
    private volatile Field  playerConnectionField;
    /** ServerCommonPacketListenerImpl.connection → Connection */
    private volatile Field  networkManagerField;
    /** Connection.channel → io.netty.channel.Channel */
    private volatile Field  channelField;

    // -----------------------------------------------------------------------

    private record FilterEntry(
            String pluginId, String category, String location, List<FilterRule> rules
    ) {
        boolean matches(String plainText) {
            return rules.stream().anyMatch(r -> r.matches(plainText));
        }
    }

    private final PlayerSettingsService settingsService;
    private volatile List<FilterEntry>  entries = List.of();

    public ExternalMessageFilter(PlayerSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    // -----------------------------------------------------------------------
    // Reload
    // -----------------------------------------------------------------------

    /**
     * Replaces the active rule set with the supplied map.
     * Safe to call from any thread.
     *
     * @param rules storageKey → list of {@link FilterRule}s
     */
    public void reload(Map<String, List<FilterRule>> rules) {
        List<FilterEntry> newEntries = new ArrayList<>(rules.size());
        for (Map.Entry<String, List<FilterRule>> e : rules.entrySet()) {
            String[] parts = e.getKey().split("\\.", 3);
            if (parts.length == 3) {
                newEntries.add(new FilterEntry(parts[0], parts[1], parts[2], List.copyOf(e.getValue())));
            }
        }
        this.entries = List.copyOf(newEntries);
    }

    // -----------------------------------------------------------------------
    // Netty pipeline injection
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent event) {
        inject(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        uninject(event.getPlayer());
    }

    /** Injects the filter handler into the player's Netty pipeline. */
    public void inject(Player player) {
        Channel channel = getChannel(player);
        if (channel == null) return;
        if (channel.pipeline().get(HANDLER_NAME) != null) return; // already present

        channel.pipeline().addBefore("packet_handler", HANDLER_NAME,
                new ChannelDuplexHandler() {
                    @Override
                    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise)
                            throws Exception {
                        if (shouldBlock(player, msg)) {
                            promise.setSuccess(); // drop packet silently
                        } else {
                            super.write(ctx, msg, promise);
                        }
                    }
                });
    }

    /** Removes the filter handler from the player's Netty pipeline. */
    public void uninject(Player player) {
        Channel channel = getChannel(player);
        if (channel != null && channel.pipeline().get(HANDLER_NAME) != null) {
            channel.pipeline().remove(HANDLER_NAME);
        }
    }

    // -----------------------------------------------------------------------
    // Packet inspection
    // -----------------------------------------------------------------------

    /**
     * Returns {@code true} when the given packet should be dropped for
     * {@code player} based on the active filter rules and player preferences.
     */
    boolean shouldBlock(Player player, Object msg) {
        if (entries.isEmpty()) return false;
        if (AS_ADVENTURE_METHOD == null) return false;

        try {
            if (SYSTEM_CHAT_CLASS != null && SYSTEM_CHAT_CLASS.isInstance(msg)) {
                boolean overlay = (boolean) OVERLAY_METHOD.invoke(msg);
                String category = overlay ? "actionbars" : "messages";
                return checkFilters(player, category, CONTENT_METHOD.invoke(msg));
            }
            if (ACTION_BAR_CLASS != null && ACTION_BAR_CLASS.isInstance(msg)) {
                return checkFilters(player, "actionbars", TEXT_METHOD.invoke(msg));
            }
        } catch (Exception ignored) {}
        return false;
    }

    private boolean checkFilters(Player player, String category, Object nmsComponent) {
        Component component;
        try {
            component = (Component) AS_ADVENTURE_METHOD.invoke(null, nmsComponent);
        } catch (Exception ignored) {
            return false;
        }
        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        for (FilterEntry entry : entries) {
            if (!entry.category().equals(category)) continue;
            if (entry.matches(plain)
                    && !settingsService.isEnabled(player, entry.pluginId(), entry.category(), entry.location())) {
                return true;
            }
        }
        return false;
    }

    // -----------------------------------------------------------------------
    // Channel access via reflection
    // -----------------------------------------------------------------------

    private Channel getChannel(Player player) {
        try {
            // CraftPlayer.getHandle() → ServerPlayer
            if (getHandleMethod == null) {
                getHandleMethod = player.getClass().getMethod("getHandle");
            }
            Object serverPlayer = getHandleMethod.invoke(player);

            // ServerPlayer.connection → ServerGamePacketListenerImpl
            if (playerConnectionField == null) {
                playerConnectionField = serverPlayer.getClass().getField("connection");
            }
            Object packetListener = playerConnectionField.get(serverPlayer);

            // ServerCommonPacketListenerImpl.connection → Connection
            if (networkManagerField == null) {
                networkManagerField = packetListener.getClass().getField("connection");
            }
            Object connection = networkManagerField.get(packetListener);

            // Connection.channel → io.netty.channel.Channel (public field)
            if (channelField == null) {
                channelField = connection.getClass().getField("channel");
            }
            return (Channel) channelField.get(connection);
        } catch (Exception ignored) {
            return null;
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Class<?> tryClass(String name) {
        try { return Class.forName(name); }
        catch (ClassNotFoundException ignored) { return null; }
    }

    private static Method tryMethod(Class<?> clazz, String name) {
        if (clazz == null) return null;
        try { return clazz.getMethod(name); }
        catch (NoSuchMethodException ignored) { return null; }
    }
}
