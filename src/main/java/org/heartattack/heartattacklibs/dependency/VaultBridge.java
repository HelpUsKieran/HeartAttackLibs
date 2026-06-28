package org.heartattack.heartattacklibs.dependency;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.heartattack.heartattacklibs.dependency.provider.ChatProvider;
import org.heartattack.heartattacklibs.dependency.provider.EconomyProvider;
import org.heartattack.heartattacklibs.dependency.provider.NoOpChatProvider;
import org.heartattack.heartattacklibs.dependency.provider.NoOpEconomyProvider;
import org.heartattack.heartattacklibs.dependency.provider.NoOpPermissionProvider;
import org.heartattack.heartattacklibs.dependency.provider.PermissionProvider;
import org.heartattack.heartattacklibs.dependency.vault.VaultChatProvider;
import org.heartattack.heartattacklibs.dependency.vault.VaultEconomyProvider;
import org.heartattack.heartattacklibs.dependency.vault.VaultPermissionProvider;

public final class VaultBridge {
    private final JavaPlugin plugin;
    private EconomyProvider economyProvider = new NoOpEconomyProvider();
    private PermissionProvider permissionProvider = new NoOpPermissionProvider();
    private ChatProvider chatProvider = new NoOpChatProvider();

    public VaultBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public DependencyStatus initialize() {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            return DependencyStatus.unavailable("Vault plugin not found.");
        }

        RegisteredServiceProvider<Economy> econRegistration = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (econRegistration != null) {
            economyProvider = new VaultEconomyProvider(econRegistration.getProvider());
        }

        RegisteredServiceProvider<Permission> permissionRegistration = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (permissionRegistration != null) {
            permissionProvider = new VaultPermissionProvider(permissionRegistration.getProvider());
        }

        RegisteredServiceProvider<Chat> chatRegistration = Bukkit.getServicesManager().getRegistration(Chat.class);
        if (chatRegistration != null) {
            chatProvider = new VaultChatProvider(chatRegistration.getProvider());
        }

        if (!economyProvider.available() && !permissionProvider.available() && !chatProvider.available()) {
            return DependencyStatus.unavailable("Vault found but no service providers were registered.");
        }

        plugin.getLogger().info("Vault bridge ready: economy=" + economyProvider.available()
                + ", permission=" + permissionProvider.available()
                + ", chat=" + chatProvider.available());
        return DependencyStatus.available("Vault service providers resolved.");
    }

    public EconomyProvider economyProvider() {
        return economyProvider;
    }

    public PermissionProvider permissionProvider() {
        return permissionProvider;
    }

    public ChatProvider chatProvider() {
        return chatProvider;
    }
}
