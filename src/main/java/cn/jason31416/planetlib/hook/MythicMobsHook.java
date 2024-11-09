package cn.jason31416.planetlib.hook;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class MythicMobsHook {
    public static boolean enabled = false;
    public static void init() {
        if(Bukkit.getPluginManager().getPlugin("MythicMobs") == null) {
            Bukkit.getLogger().info("\033[31mFailed to hook MythicMobs\033[0m");
            enabled = false;
            return;
        }
        Bukkit.getLogger().info("\033[32mHook MythicMobs successfully\033[0m");
        enabled = true;
    }
    @Nullable
    public static ActiveMob spawnMob(String mobName, SimpleLocation position, @Nullable Integer level){
        if(level == null) level = 1;
        if(!enabled) {
            return null;
        }
        try (MythicBukkit mythicBukkit = MythicBukkit.inst()) {
            MythicMob mob = mythicBukkit.getMobManager().getMythicMob(mobName).orElse(null);
            if(mob != null){
                return mob.spawn(BukkitAdapter.adapt(position.getBukkitLocation()), level);
            }else{
                PlanetLib.instance.getLogger().severe("ERROR: Unable to find mob: "+mobName+"!");
            }
        }
        return null;
    }
    public static Collection<String> loadedMobTypes() {
        if(!enabled) {
            return Collections.emptySet();
        }
        try (MythicBukkit mythicBukkit = MythicBukkit.inst()) {
            return mythicBukkit.getMobManager().getMobNames();
        }
    }
}
