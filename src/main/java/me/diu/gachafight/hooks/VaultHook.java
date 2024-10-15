package me.diu.gachafight.hooks;

import me.diu.gachafight.playerstats.leaderboard.LeaderboardUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;

public class VaultHook {
    private static Economy econ = null;
    private static final DecimalFormat df = new DecimalFormat("#,##0.00");

    public static boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static boolean isEconomySetup() {
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static double getBalance(OfflinePlayer player) {
        if (!isEconomySetup()) {
            throw new UnsupportedOperationException("Vault Economy not found");
        }
        return econ.getBalance(player);
    }

    public static String getFormattedBalance(OfflinePlayer player) {
        return df.format(getBalance(player));
    }

    public static String withdraw(OfflinePlayer player, double amount) {
        if (!isEconomySetup()) {
            throw new UnsupportedOperationException("Vault Economy not found");
        }
        EconomyResponse response = econ.withdrawPlayer(player, amount);
        return response.transactionSuccess() ? "" : response.errorMessage;
    }

    public static String deposit(OfflinePlayer player, double amount) {
        if (!isEconomySetup()) {
            throw new UnsupportedOperationException("Vault Economy not found");
        }
        EconomyResponse response = econ.depositPlayer(player, amount);
        return response.transactionSuccess() ? "" : response.errorMessage;
    }

    public static String format(double amount) {
        if (!isEconomySetup()) {
            throw new UnsupportedOperationException("Vault Economy not found");
        }
        return econ.format(amount);
    }

    public static String formatTwoDecimals(double amount) {
        return df.format(amount);
    }

    public static void setMoney(OfflinePlayer player, double amount) {
        if (!isEconomySetup()) {
            throw new UnsupportedOperationException("Vault Economy not found");
        }
        double currentBalance = econ.getBalance(player);
        if (currentBalance > amount) {
            econ.withdrawPlayer(player, currentBalance - amount);
        } else if (currentBalance < amount) {
            econ.depositPlayer(player, amount - currentBalance);
        }
        // If currentBalance == amount, do nothing
    }
    public static boolean addMoney(OfflinePlayer player, double amount) {
        if (!isEconomySetup()) {
            throw new UnsupportedOperationException("Vault Economy not found");
        }
        EconomyResponse response = econ.depositPlayer(player, amount);
        LeaderboardUtils.markPlayerDirty(player.getUniqueId());
        return response.transactionSuccess();
    }
}
