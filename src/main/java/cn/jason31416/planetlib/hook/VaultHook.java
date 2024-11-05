package cn.jason31416.planetlib.hook;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.planetlib.PlanetLib;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class VaultHook {
    private static RegisteredServiceProvider<Economy> rsp;
    public static void init() {
        if(Bukkit.getPluginManager().getPlugin("Vault") == null || Bukkit.getServer().getServicesManager().getRegistration(Economy.class) == null) {
            Bukkit.getLogger().info("\033[31mFailed to hook Vault, disabling plugin...\033[0m");
            Bukkit.getPluginManager().disablePlugin(PlanetLib.instance);
            return;
        }
        rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        Bukkit.getLogger().info("\033[32mHook Vault successfully\033[0m");
    }
    public static void end() {
    }
    public static double getBalance(@Nonnull OfflinePlayer player) {
        return rsp.getProvider().getBalance(player);
    }
    public static void depositBalance(@Nonnull OfflinePlayer player, double balance) {
        rsp.getProvider().depositPlayer(player, balance);
    }
    public static boolean withdrawBalance(@Nonnull OfflinePlayer player, double balance) {
        if(getBalance(player) < balance) return false;
        rsp.getProvider().withdrawPlayer(player, balance);
        return true;
    }
    public static boolean haveBalance(@Nonnull OfflinePlayer player, double balance) {
        return getBalance(player) >= balance;
    }
    public static void setBalance(@Nonnull OfflinePlayer player, double balance) {
        if (getBalance(player) >= balance) {
            withdrawBalance(player, getBalance(player) - balance);
        } else {
            depositBalance(player, balance - getBalance(player));
        }
    }
}
