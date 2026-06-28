package org.heartattack.heartattacklibs.dependency.provider;

import org.bukkit.entity.Player;

public final class NoOpEconomyProvider implements EconomyProvider {
    @Override
    public boolean available() {
        return false;
    }

    @Override
    public double balance(Player player) {
        return 0.0d;
    }

    @Override
    public boolean deposit(Player player, double amount) {
        return false;
    }

    @Override
    public boolean withdraw(Player player, double amount) {
        return false;
    }

    @Override
    public String currencyName() {
        return "money";
    }
}
