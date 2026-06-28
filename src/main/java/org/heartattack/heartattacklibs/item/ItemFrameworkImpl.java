package org.heartattack.heartattacklibs.item;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.heartattack.heartattacklibs.util.MaterialResolver;

import java.util.List;

public final class ItemFrameworkImpl implements ItemFramework {
    @Override
    public ItemBuilder builder(Material material) {
        return ItemBuilder.of(material);
    }

    @Override
    public ItemStack build(ConfigurationSection section, Material fallbackMaterial) {
        Material material = fallbackMaterial;
        if (section.contains("material")) {
            Material parsed = MaterialResolver.parseModern(section.getString("material", fallbackMaterial.name()));
            if (parsed != null) {
                material = parsed;
            }
        }

        ItemBuilder builder = ItemBuilder.of(material).amount(section.getInt("amount", 1));

        if (section.contains("name")) {
            builder.name(section.getString("name", ""));
        }

        List<String> lore = section.getStringList("lore");
        if (!lore.isEmpty()) {
            builder.lore(lore);
        }

        if (section.getBoolean("glow", false)) {
            builder.glow();
        }

        if (section.contains("custom-model-data")) {
            builder.customModelData(section.getInt("custom-model-data"));
        }

        if (section.isConfigurationSection("texture")) {
            ConfigurationSection texture = section.getConfigurationSection("texture");
            builder.texture(texture.getString("type", "url"), texture.getString("value", ""));
        } else {
            String legacyTexture = section.getString("texture", "");
            if (!legacyTexture.isBlank()) {
                builder.texture(legacyTexture);
            }
        }

        return builder.build();
    }
}
