package org.heartattack.heartattacklibs.dependency.vault;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.heartattack.heartattacklibs.dependency.provider.EconomyProvider;

public final class VaultEconomyProvider implements EconomyProvider {
    private final Economy economy;

    public VaultEconomyProvider(Economy economy) {
        this.economy = economy;
    }

    @Override
    public boolean available() {
        return economy != null;
    }

    @Override
    public double balance(Player player) {
        return economy.getBalance(player);
    }

    @Override
    public boolean deposit(Player player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public String currencyName() {
        return economy.currencyNamePlural();
    }
}
