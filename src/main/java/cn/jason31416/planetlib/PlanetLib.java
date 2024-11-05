package cn.jason31416.planetlib;

import cn.jason31416.planetlib.command.RootCommand;
import cn.jason31416.planetlib.command.tempAction.PlanetLibRootCommand;
import cn.jason31416.planetlib.command.tempAction.TempAction;
import cn.jason31416.planetlib.gui.GUIEventHandler;
import cn.jason31416.planetlib.hook.VaultHook;
import cn.jason31416.planetlib.message.InternalPlaceholder;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.update.UpdateCycle;
import cn.jason31416.planetlib.update.UpdateTask;
import io.lumine.mythic.api.packs.Pack;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class PlanetLib {
    static boolean initialized = false;
    public static JavaPlugin instance;
    public static String packageName;
    public static boolean isInitialized() {
        return initialized;
    }
    public static void initialize(JavaPlugin plugin) {
        if (initialized) {
            return;
        }
        instance = plugin;
        VaultHook.init();
        String pkg = Bukkit.getServer().getClass().getPackage().getName();
        packageName = pkg.substring(pkg.lastIndexOf(".")+1);
        plugin.saveDefaultConfig();
        initialized = true;
        Config.start(plugin);
        UpdateCycle.start();
        MessageLoader.initialize(new File(plugin.getDataFolder(), "lang/"+Config.getString("lang")+".yml"), plugin);
        new PlanetLibRootCommand().register();

        UpdateCycle.registerTask("PlanetLib.tempActionUpdater", new UpdateTask(60*20, TempAction::checkAll));
        instance.getServer().getPluginManager().registerEvents(new GUIEventHandler(), plugin);
    }
    public static void reload(JavaPlugin plugin) {
        Config.start(plugin);
        plugin.saveDefaultConfig();
        MessageLoader.initialize(new File(plugin.getDataFolder(), "lang/"+Config.getString("lang")), plugin);
        InternalPlaceholder.placeholderHandlers.clear();
    }
    public static void shutdown() {
        UpdateCycle.stop();
        MessageLoader.close();
        initialized = false;
    }
}
