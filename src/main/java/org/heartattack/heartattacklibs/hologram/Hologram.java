package org.heartattack.heartattacklibs.hologram;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Hologram {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private Location baseLocation;
    private final List<String> lines;
    private final double lineSpacing;
    private final Display.Billboard billboard;
    private final float viewRange;
    private final boolean seeThrough;
    private final boolean shadowed;
    private final Color backgroundColor;
    private ItemStack icon;
    private final double iconOffsetY;
    private final double iconScale;
    private final List<TextDisplay> spawnedDisplays = new ArrayList<>();
    private ItemDisplay iconDisplay;

    Hologram(
            Location baseLocation,
            List<String> lines,
            double lineSpacing,
            Display.Billboard billboard,
            float viewRange,
            boolean seeThrough,
            boolean shadowed,
            Color backgroundColor,
            ItemStack icon,
            double iconOffsetY,
            double iconScale
    ) {
        this.baseLocation = baseLocation.clone();
        this.lines = new ArrayList<>(lines);
        this.lineSpacing = lineSpacing;
        this.billboard = billboard;
        this.viewRange = viewRange;
        this.seeThrough = seeThrough;
        this.shadowed = shadowed;
        this.backgroundColor = backgroundColor;
        this.icon = icon == null ? null : icon.clone();
        this.iconOffsetY = iconOffsetY;
        this.iconScale = iconScale;
    }

    public void spawn() {
        despawn();
        World world = baseLocation.getWorld();
        if (world == null) {
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            Location lineLocation = baseLocation.clone().subtract(0.0, i * lineSpacing, 0.0);
            String text = lines.get(i);

            TextDisplay display = world.spawn(lineLocation, TextDisplay.class, entity -> {
                entity.text(MINI_MESSAGE.deserialize(text));
                entity.setBillboard(billboard);
                entity.setViewRange(viewRange);
                entity.setSeeThrough(seeThrough);
                entity.setShadowed(shadowed);
                if (backgroundColor != null) {
                    entity.setDefaultBackground(false);
                    entity.setBackgroundColor(backgroundColor);
                }
                entity.setGravity(false);
                entity.setPersistent(false);
            });
            spawnedDisplays.add(display);
        }

        if (icon != null) {
            iconDisplay = world.spawn(iconLocation(), ItemDisplay.class, entity -> {
                entity.setItemStack(icon);
                entity.setBillboard(billboard);
                entity.setViewRange(viewRange);
                entity.setGravity(false);
                entity.setPersistent(false);
                entity.setTransformation(iconTransformation());
            });
        }
    }

    public void despawn() {
        for (TextDisplay display : spawnedDisplays) {
            if (display != null && display.isValid()) {
                display.remove();
            }
        }
        spawnedDisplays.clear();
        if (iconDisplay != null && iconDisplay.isValid()) {
            iconDisplay.remove();
        }
        iconDisplay = null;
    }

    public boolean isSpawned() {
        return !spawnedDisplays.isEmpty() || iconDisplay != null;
    }

    public void teleport(Location newBaseLocation) {
        this.baseLocation = newBaseLocation.clone();
        if (!isSpawned()) {
            return;
        }

        for (int i = 0; i < spawnedDisplays.size(); i++) {
            TextDisplay display = spawnedDisplays.get(i);
            if (display != null && display.isValid()) {
                display.teleport(baseLocation.clone().subtract(0.0, i * lineSpacing, 0.0));
            }
        }
        if (iconDisplay != null && iconDisplay.isValid()) {
            iconDisplay.teleport(iconLocation());
        }
    }

    public void updateLine(int index, String newText) {
        if (index < 0 || index >= lines.size()) {
            throw new IllegalArgumentException("Line index out of bounds: " + index);
        }
        lines.set(index, newText);

        if (isSpawned() && index < spawnedDisplays.size()) {
            TextDisplay display = spawnedDisplays.get(index);
            if (display != null && display.isValid()) {
                display.text(MINI_MESSAGE.deserialize(newText));
            }
        }
    }

    /** Replaces the floating icon item (no-op if this hologram was built without an icon). */
    public void updateIcon(ItemStack newIcon) {
        this.icon = newIcon == null ? null : newIcon.clone();
        if (iconDisplay != null && iconDisplay.isValid() && this.icon != null) {
            iconDisplay.setItemStack(this.icon);
        }
    }

    public Location baseLocation() {
        return baseLocation.clone();
    }

    public List<String> lines() {
        return Collections.unmodifiableList(lines);
    }

    /** The spawned icon entity, or {@code null} if no icon was configured / not yet spawned. */
    public ItemDisplay iconEntity() {
        return iconDisplay;
    }

    /** The spawned text-line entities (empty until {@link #spawn()}). */
    public List<TextDisplay> textEntities() {
        return Collections.unmodifiableList(spawnedDisplays);
    }

    private Location iconLocation() {
        return baseLocation.clone().add(0.0, iconOffsetY, 0.0);
    }

    private Transformation iconTransformation() {
        float scale = (float) Math.max(0.05, iconScale);
        return new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(scale, scale, scale), new Quaternionf());
    }
}
