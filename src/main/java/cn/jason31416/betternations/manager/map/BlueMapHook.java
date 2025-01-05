package cn.jason31416.betternations.manager.map;

import cn.jason31416.betternations.BetterNations;
import org.bukkit.Bukkit;

public class BlueMapHook {
    public static boolean enabled = false;
    public static void init() {
        if(Bukkit.getPluginManager().getPlugin("BlueMap") == null) {
            BetterNations.instance.getLogger().info("\033[31mFailed to hook BlueMap\033[0m");
            enabled = false;
            return;
        }
        BetterNations.instance.getLogger().info("\033[32mFound BlueMap, attempting to hook...\033[0m");
        enabled = true;
    }
}
