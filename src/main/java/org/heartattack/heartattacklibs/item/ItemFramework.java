package org.heartattack.heartattacklibs.item;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public interface ItemFramework {
    ItemBuilder builder(Material material);

    ItemStack build(ConfigurationSection section, Material fallbackMaterial);
}
