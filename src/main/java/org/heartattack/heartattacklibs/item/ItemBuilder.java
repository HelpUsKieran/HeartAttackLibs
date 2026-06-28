package org.heartattack.heartattacklibs.item;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public final class ItemBuilder {
    private static final Logger LOGGER = Logger.getLogger(ItemBuilder.class.getName());

    private ItemStack itemStack;
    private boolean textured;

    private ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder name(String miniMessageName) {
        return editMeta(meta -> meta.displayName(MiniMessageItem.text(miniMessageName)));
    }

    public ItemBuilder guiName(String miniMessageName) {
        return editMeta(meta -> meta.displayName(MiniMessageItem.guiText(miniMessageName)));
    }

    public ItemBuilder lore(List<String> miniMessageLore) {
        return editMeta(meta -> {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String line : miniMessageLore) {
                lore.add(MiniMessageItem.text(line));
            }
            meta.lore(lore);
        });
    }

    public ItemBuilder guiLore(List<String> miniMessageLore) {
        return editMeta(meta -> {
            List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
            for (String line : miniMessageLore) {
                lore.add(MiniMessageItem.guiText(line));
            }
            meta.lore(lore);
        });
    }

    public ItemBuilder lore(String... miniMessageLore) {
        return lore(Arrays.asList(miniMessageLore));
    }

    public ItemBuilder guiLore(String... miniMessageLore) {
        return guiLore(Arrays.asList(miniMessageLore));
    }

    public ItemBuilder addLoreLine(String miniMessageLine) {
        return editMeta(meta -> {
            List<net.kyori.adventure.text.Component> lore = meta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            } else {
                lore = new ArrayList<>(lore);
            }
            lore.add(MiniMessageItem.text(miniMessageLine));
            meta.lore(lore);
        });
    }

    public ItemBuilder addGuiLoreLine(String miniMessageLine) {
        return editMeta(meta -> {
            List<net.kyori.adventure.text.Component> lore = meta.lore();
            if (lore == null) {
                lore = new ArrayList<>();
            } else {
                lore = new ArrayList<>(lore);
            }
            lore.add(MiniMessageItem.guiText(miniMessageLine));
            meta.lore(lore);
        });
    }

    public ItemBuilder enchant(Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        return editMeta(meta -> meta.addEnchant(enchantment, level, ignoreLevelRestriction));
    }

    public ItemBuilder flags(ItemFlag... flags) {
        return editMeta(meta -> meta.addItemFlags(flags));
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        return editMeta(meta -> meta.setUnbreakable(unbreakable));
    }

    public ItemBuilder glow() {
        return enchant(Enchantment.UNBREAKING, 1, true).flags(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder customModelData(int customModelData) {
        if (textured) {
            return this;
        }
        return editMeta(meta -> meta.setCustomModelData(customModelData));
    }

    public ItemBuilder texture(String texture) {
        if (!ItemTextureSupport.isTextured(texture)) {
            return this;
        }
        this.itemStack = ItemTextureSupport.applyTexture(itemStack, "url", texture, LOGGER);
        this.textured = true;
        return this;
    }

    public ItemBuilder texture(String type, String value) {
        if (!ItemTextureSupport.isTextured(value)) {
            return this;
        }
        this.itemStack = ItemTextureSupport.applyTexture(itemStack, type, value, LOGGER);
        this.textured = true;
        return this;
    }

    public ItemBuilder textureBase64(String value) {
        return texture("base64", value);
    }

    public ItemBuilder texturePlayer(String value) {
        return texture("player", value);
    }

    public ItemStack build() {
        return itemStack.clone();
    }

    private ItemBuilder editMeta(MetaEditor editor) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return this;
        }
        editor.edit(meta);
        itemStack.setItemMeta(meta);
        return this;
    }

    @FunctionalInterface
    private interface MetaEditor {
        void edit(ItemMeta meta);
    }
}
