package org.heartattack.heartattacklibs.settings;

import org.bukkit.Material;

public record SettingsDisplayConfig(
        String enabledSymbol,
        String enabledColor,
        String enabledLabel,
        Material enabledMaterial,
        String disabledSymbol,
        String disabledColor,
        String disabledLabel,
        Material disabledMaterial
) {
    public static SettingsDisplayConfig defaults() {
        return new SettingsDisplayConfig(
                "✓", "green", "Enabled", Material.LIME_DYE,
                "✗", "red", "Disabled", Material.GRAY_DYE
        );
    }

    public String statusLine(boolean enabled) {
        if (enabled) {
            return "<!i><gray>Status: <" + enabledColor + ">" + enabledSymbol + " " + enabledLabel;
        }
        return "<!i><gray>Status: <" + disabledColor + ">" + disabledSymbol + " " + disabledLabel;
    }

    public String stateText(boolean enabled) {
        if (enabled) {
            return "<" + enabledColor + ">" + enabledSymbol + " " + enabledLabel;
        }
        return "<" + disabledColor + ">" + disabledSymbol + " " + disabledLabel;
    }

    public Material material(boolean enabled) {
        return enabled ? enabledMaterial : disabledMaterial;
    }
}
