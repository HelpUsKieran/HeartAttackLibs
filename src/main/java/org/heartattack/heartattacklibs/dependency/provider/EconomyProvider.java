package org.heartattack.heartattacklibs.dependency.provider;

import org.bukkit.entity.Player;

public interface EconomyProvider {
    boolean available();

    double balance(Player player);

    boolean deposit(Player player, double amount);

    boolean withdraw(Player player, double amount);

    String currencyName();
}
