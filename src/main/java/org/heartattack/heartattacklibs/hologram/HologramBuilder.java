package org.heartattack.heartattacklibs.hologram;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class HologramBuilder {
    private final Location baseLocation;
    private final List<String> lines = new ArrayList<>();
    private double lineSpacing = 0.28;
    private Display.Billboard billboard = Display.Billboard.CENTER;
    private float viewRange = 32.0f;
    private boolean seeThrough = true;
    private boolean shadowed = false;
    private Color backgroundColor = null;
    private ItemStack icon = null;
    private double iconOffsetY = 0.0;
    private double iconScale = 0.5;

    private HologramBuilder(Location baseLocation) {
        this.baseLocation = baseLocation.clone();
    }

    public static HologramBuilder at(Location baseLocation) {
        return new HologramBuilder(baseLocation);
    }

    public HologramBuilder line(String text) {
        this.lines.add(text);
        return this;
    }

    public HologramBuilder lines(String... textLines) {
        this.lines.addAll(Arrays.asList(textLines));
        return this;
    }

    public HologramBuilder lineSpacing(double lineSpacing) {
        this.lineSpacing = lineSpacing;
        return this;
    }

    public HologramBuilder billboard(Display.Billboard billboard) {
        this.billboard = billboard;
        return this;
    }

    public HologramBuilder viewRange(float viewRange) {
        this.viewRange = viewRange;
        return this;
    }

    public HologramBuilder seeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
        return this;
    }

    public HologramBuilder shadowed(boolean shadowed) {
        this.shadowed = shadowed;
        return this;
    }

    /** Sets a semi-transparent background panel behind the text (alpha baked into the color). */
    public HologramBuilder background(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /** Adds a floating item icon to the hologram (null = no icon). */
    public HologramBuilder icon(ItemStack icon) {
        this.icon = icon;
        return this;
    }

    /** Vertical offset of the icon relative to the hologram base location. */
    public HologramBuilder iconOffsetY(double iconOffsetY) {
        this.iconOffsetY = iconOffsetY;
        return this;
    }

    /** Uniform scale applied to the icon item display. */
    public HologramBuilder iconScale(double iconScale) {
        this.iconScale = iconScale;
        return this;
    }

    public Hologram build() {
        return new Hologram(baseLocation, lines, lineSpacing, billboard, viewRange, seeThrough, shadowed,
                backgroundColor, icon, iconOffsetY, iconScale);
    }
}
