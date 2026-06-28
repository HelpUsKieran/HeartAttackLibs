package org.heartattack.heartattacklibs.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.heartattack.heartattacklibs.text.UnicodeSmallCaps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A code-built, interactive ("live") inventory GUI — the dynamic counterpart to the YAML menu
 * framework. Unlike config menus (which cancel every click), a {@code LiveMenu} supports:
 *
 * <ul>
 *   <li><b>Buttons</b> — clickable slots with a code callback; the click is auto-cancelled.</li>
 *   <li><b>Input regions</b> — slot groups players may freely place/take items in, optionally
 *       restricted to specific players (e.g. each side of a trade).</li>
 *   <li><b>Lifecycle hooks</b> — input-change, close, and an optional repeating refresh.</li>
 *   <li><b>Shared viewers</b> — multiple players can view/edit the same menu live.</li>
 * </ul>
 *
 * Anti-duplication handling (cancel non-interactive clicks, block collect-to-cursor, route
 * shift-clicks/drags only into a player's own input region) is built in. Obtain a builder via
 * {@code HeartAttackLibsApi.gui().liveMenu(title, rows)}.
 */
public final class LiveMenu implements InventoryHolder {
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final Plugin plugin;
    private final Inventory inventory;
    private final Map<Integer, Consumer<ClickContext>> buttons;
    private final List<InputRegion> regions;
    private final Map<Integer, InputRegion> slotRegion;
    private final BiConsumer<Player, String> onInputChange;
    private final Consumer<Player> onClose;
    private final int refreshTicks;
    private final Consumer<LiveMenu> onRefresh;

    private BukkitTask refreshTask;
    private boolean disposed;

    private LiveMenu(Plugin plugin, Inventory inventory, Map<Integer, Consumer<ClickContext>> buttons,
                     List<InputRegion> regions, BiConsumer<Player, String> onInputChange,
                     Consumer<Player> onClose, int refreshTicks, Consumer<LiveMenu> onRefresh) {
        this.plugin = plugin;
        this.inventory = inventory;
        this.buttons = buttons;
        this.regions = regions;
        this.onInputChange = onInputChange;
        this.onClose = onClose;
        this.refreshTicks = refreshTicks;
        this.onRefresh = onRefresh;
        this.slotRegion = new HashMap<>();
        for (InputRegion region : regions) {
            for (int slot : region.slots()) {
                slotRegion.put(slot, region);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
        startRefresh();
    }

    public void openShared(Player... players) {
        for (Player player : players) {
            player.openInventory(inventory);
        }
        startRefresh();
    }

    public void setItem(int slot, ItemStack item) {
        if (slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, item);
        }
    }

    public ItemStack getItem(int slot) {
        return inventory.getItem(slot);
    }

    /** Collects (and optionally clears) the items currently in a named input region. */
    public List<ItemStack> itemsIn(String region, boolean clear) {
        List<ItemStack> items = new ArrayList<>();
        for (InputRegion candidate : regions) {
            if (!candidate.name().equals(region)) {
                continue;
            }
            for (int slot : candidate.slots()) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && !item.getType().isAir()) {
                    items.add(item);
                }
                if (clear) {
                    inventory.setItem(slot, null);
                }
            }
        }
        return items;
    }

    public List<HumanEntity> viewers() {
        return new ArrayList<>(inventory.getViewers());
    }

    /** Closes the menu for all current viewers and stops any refresh task. */
    public void closeAll() {
        for (HumanEntity viewer : viewers()) {
            viewer.closeInventory();
        }
        dispose();
    }

    public boolean disposed() {
        return disposed;
    }

    // --- Event handling (invoked by GuiFrameworkImpl) ---

    void handleClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        // Gathering matching items can pull from any slot, including other players' input regions.
        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) {
            event.setCancelled(true);
            return;
        }

        if (clicked.equals(inventory)) {
            int slot = event.getRawSlot();
            Consumer<ClickContext> button = buttons.get(slot);
            if (button != null) {
                event.setCancelled(true);
                button.accept(new ClickContext(player, this, slot, event.getClick()));
                return;
            }
            InputRegion region = slotRegion.get(slot);
            if (region != null && region.editableBy().test(player)) {
                fireInputChange(player, region.name()); // allowed; let the placement happen
                return;
            }
            event.setCancelled(true);
            return;
        }

        // Click in the player's own inventory.
        if (event.isShiftClick()) {
            event.setCancelled(true);
            shiftIntoRegion(player, event);
        }
    }

    void handleDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        int topSize = inventory.getSize();
        InputRegion dragged = null;
        for (int raw : event.getRawSlots()) {
            if (raw >= topSize) {
                continue;
            }
            InputRegion region = slotRegion.get(raw);
            if (region == null || !region.editableBy().test(player)) {
                event.setCancelled(true);
                return;
            }
            dragged = region;
        }
        if (dragged != null) {
            fireInputChange(player, dragged.name());
        }
    }

    void handleClose(InventoryCloseEvent event) {
        if (onClose != null && event.getPlayer() instanceof Player player) {
            onClose.accept(player);
        }
        if (inventory.getViewers().size() <= 1) {
            dispose();
        }
    }

    private void shiftIntoRegion(Player player, InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        InputRegion region = firstEditableRegion(player);
        if (region == null) {
            return;
        }
        ItemStack remaining = item.clone();
        for (int slot : region.slots()) {
            ItemStack current = inventory.getItem(slot);
            if (current == null || current.getType().isAir()) {
                inventory.setItem(slot, remaining);
                remaining = null;
                break;
            }
            if (current.isSimilar(remaining) && current.getAmount() < current.getMaxStackSize()) {
                int space = current.getMaxStackSize() - current.getAmount();
                int move = Math.min(space, remaining.getAmount());
                current.setAmount(current.getAmount() + move);
                inventory.setItem(slot, current);
                remaining.setAmount(remaining.getAmount() - move);
                if (remaining.getAmount() <= 0) {
                    remaining = null;
                    break;
                }
            }
        }
        event.setCurrentItem(remaining);
        fireInputChange(player, region.name());
    }

    private InputRegion firstEditableRegion(Player player) {
        for (InputRegion region : regions) {
            if (region.editableBy().test(player)) {
                return region;
            }
        }
        return null;
    }

    private void fireInputChange(Player player, String region) {
        if (onInputChange == null) {
            return;
        }
        // Run next tick so the inventory reflects the applied change.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!disposed) {
                onInputChange.accept(player, region);
            }
        });
    }

    private void startRefresh() {
        if (refreshTask != null || onRefresh == null || refreshTicks <= 0) {
            return;
        }
        refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (disposed || inventory.getViewers().isEmpty()) {
                return;
            }
            onRefresh.accept(this);
        }, refreshTicks, refreshTicks);
    }

    private void dispose() {
        disposed = true;
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    static Component renderTitle(String title) {
        String transformed = UnicodeSmallCaps.apply(title == null ? "" : title);
        Component component;
        if (transformed.contains("<") && transformed.contains(">")) {
            component = MINI.deserialize(transformed);
        } else {
            component = LEGACY.deserialize(ChatColor.translateAlternateColorCodes('&', transformed));
        }
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    /** A slot group players may edit, optionally restricted to specific players. */
    record InputRegion(String name, List<Integer> slots, Predicate<Player> editableBy) {
    }

    /** Context passed to a button's click callback. */
    public record ClickContext(Player player, LiveMenu menu, int slot,
                               org.bukkit.event.inventory.ClickType click) {
    }

    /** Fluent builder for a {@link LiveMenu}. Create via {@code HeartAttackLibsApi.gui().liveMenu(title, rows)}. */
    public static final class Builder {
        private final Plugin plugin;
        private final Inventory inventory;
        private final Map<Integer, Consumer<ClickContext>> buttons = new HashMap<>();
        private final List<InputRegion> regions = new ArrayList<>();
        private BiConsumer<Player, String> onInputChange;
        private Consumer<Player> onClose;
        private int refreshTicks;
        private Consumer<LiveMenu> onRefresh;
        private LiveMenu built;

        public Builder(Plugin plugin, String title, int rows) {
            this.plugin = plugin;
            int clampedRows = Math.max(1, Math.min(6, rows));
            this.inventory = Bukkit.createInventory(new Holder(), clampedRows * 9, renderTitle(title));
        }

        /** A static, non-interactive display item. */
        public Builder item(int slot, ItemStack item) {
            if (slot >= 0 && slot < inventory.getSize()) {
                inventory.setItem(slot, item);
            }
            return this;
        }

        public Builder fill(Iterable<Integer> slots, ItemStack item) {
            for (int slot : slots) {
                item(slot, item == null ? null : item.clone());
            }
            return this;
        }

        /** A clickable button; the click is auto-cancelled and {@code onClick} is invoked. */
        public Builder button(int slot, ItemStack icon, Consumer<ClickContext> onClick) {
            item(slot, icon);
            if (slot >= 0 && slot < inventory.getSize()) {
                buttons.put(slot, onClick);
            }
            return this;
        }

        /** Slots any viewer may place/take items in. */
        public Builder inputRegion(String name, int[] slots) {
            return inputRegion(name, slots, player -> true);
        }

        /** Slots only {@code editableBy} players may place/take items in (e.g. one side of a trade). */
        public Builder inputRegion(String name, int[] slots, Predicate<Player> editableBy) {
            List<Integer> list = new ArrayList<>();
            for (int slot : slots) {
                list.add(slot);
            }
            regions.add(new InputRegion(name, list, editableBy));
            return this;
        }

        public Builder onInputChange(BiConsumer<Player, String> handler) {
            this.onInputChange = handler;
            return this;
        }

        public Builder onClose(Consumer<Player> handler) {
            this.onClose = handler;
            return this;
        }

        public Builder refreshEvery(int ticks, Consumer<LiveMenu> handler) {
            this.refreshTicks = ticks;
            this.onRefresh = handler;
            return this;
        }

        public LiveMenu build() {
            if (built != null) {
                return built;
            }
            Map<Integer, Consumer<ClickContext>> buttonMap = new LinkedHashMap<>(buttons);
            built = new LiveMenu(plugin, inventory, buttonMap, new ArrayList<>(regions),
                    onInputChange, onClose, refreshTicks, onRefresh);
            // Re-home the inventory onto the finished LiveMenu so the framework can route events to it.
            ((Holder) inventory.getHolder()).bind(built);
            return built;
        }
    }

    /** Placeholder holder used during building, then bound to the finished menu so events route correctly. */
    static final class Holder implements InventoryHolder {
        private LiveMenu menu;

        void bind(LiveMenu menu) {
            this.menu = menu;
        }

        LiveMenu menu() {
            return menu;
        }

        @Override
        public Inventory getInventory() {
            return menu == null ? null : menu.getInventory();
        }
    }
}
