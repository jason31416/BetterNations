package cn.jason31416.planetlib.hook;

import cn.jason31416.planetlib.PlanetLib;
import org.bukkit.Bukkit;

public class PAPIHook {
    public static boolean enabled = false;
    public static void init() {
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            PlanetLib.instance.getLogger().info("\033[31mFailed to hook PlaceholderAPI\033[0m");
            enabled = false;
            return;
        }
        PlanetLib.instance.getLogger().info("\033[32mHooked PlaceholderAPI successfully\033[0m");
        enabled = true;
    }
}
