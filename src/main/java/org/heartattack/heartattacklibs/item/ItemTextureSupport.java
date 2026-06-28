package org.heartattack.heartattacklibs.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Logger;

final class ItemTextureSupport {
    private ItemTextureSupport() {
    }

    static ItemStack applyTexture(ItemStack source, String type, String value, Logger logger) {
        if (!isTextured(value)) {
            return source;
        }

        ItemStack textured = new ItemStack(Material.PLAYER_HEAD, source.getAmount());
        ItemMeta sourceMeta = source.getItemMeta();
        ItemMeta rawMeta = textured.getItemMeta();
        if (!(rawMeta instanceof SkullMeta meta)) {
            return textured;
        }

        copyCommonMeta(sourceMeta, meta);

        try {
            String normalizedType = type == null ? "url" : type.toLowerCase(Locale.ROOT);
            switch (normalizedType) {
                case "base64" -> applyBase64(meta, value, logger);
                case "player" -> applyPlayer(meta, value);
                default -> applyUrl(meta, value, logger);
            }
        } catch (Exception ex) {
            logger.warning("Invalid skull texture '" + value + "': " + ex.getMessage());
        }

        textured.setItemMeta(meta);
        return textured;
    }

    static boolean isTextured(String texture) {
        return texture != null && !texture.isBlank();
    }

    private static void copyCommonMeta(ItemMeta sourceMeta, ItemMeta targetMeta) {
        if (sourceMeta == null) {
            return;
        }
        if (sourceMeta.hasDisplayName()) {
            targetMeta.displayName(sourceMeta.displayName());
        }
        if (sourceMeta.hasLore()) {
            targetMeta.lore(sourceMeta.lore());
        }
        if (sourceMeta.isUnbreakable()) {
            targetMeta.setUnbreakable(true);
        }
        if (!sourceMeta.getEnchants().isEmpty()) {
            sourceMeta.getEnchants().forEach((enchantment, level) -> targetMeta.addEnchant(enchantment, level, true));
        }
        if (!sourceMeta.getItemFlags().isEmpty()) {
            targetMeta.addItemFlags(sourceMeta.getItemFlags().toArray(ItemFlag[]::new));
        }
    }

    private static UUID textureProfileId(String texture) {
        return UUID.nameUUIDFromBytes(texture.getBytes(StandardCharsets.UTF_8));
    }

    private static void applyUrl(SkullMeta meta, String texture, Logger logger) {
        applyBase64(meta, encodeTextureUrl(texture), logger);
    }

    private static void applyBase64(SkullMeta meta, String base64, Logger logger) {
        UUID profileId = textureProfileId(base64);
        org.bukkit.profile.PlayerProfile baseProfile = Bukkit.createProfile(profileId, profileId.toString().substring(0, 16));
        if (!(baseProfile instanceof PlayerProfile profile)) {
            logger.warning("Could not apply skull texture; profile implementation does not support custom properties.");
            return;
        }
        profile.setProperty(new ProfileProperty("textures", base64));
        meta.setOwnerProfile(profile);
    }

    private static void applyPlayer(SkullMeta meta, String playerName) {
        // createProfile avoids the blocking Mojang lookup of getOfflinePlayer(String).
        meta.setOwnerProfile(Bukkit.createProfile(playerName));
    }

    private static String encodeTextureUrl(String texture) {
        String payload = "{\"textures\":{\"SKIN\":{\"url\":\"" + texture + "\"}}}";
        return Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }
}
