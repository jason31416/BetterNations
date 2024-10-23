package cn.jason31416.betternations;

import cn.jason31416.betternations.command.BetterNationsCommand;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.betternations.structure.Hologram;
import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.data.DataList;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.data.YamlStorage;
import cn.jason31416.planetlib.gui.GUILoader;
import cn.jason31416.planetlib.update.UpdateCycle;
import cn.jason31416.planetlib.update.UpdateTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class BetterNations extends JavaPlugin {
    public static BetterNations instance;
    public static YamlStorage storage;

    public void saveAllResources() {
        savePluginResource("gui/test.yml");
        savePluginResource("lang/zh_cn.yml");
    }
    public void savePluginResource(@NotNull String resourcePath) {
        if (!resourcePath.isEmpty()) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = this.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + getFile());
            } else {
                File outFile = new File(getDataFolder(), resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(getDataFolder(), resourcePath.substring(0, Math.max(lastIndex, 0)));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }
                try {
                    if (!outFile.exists()) {
                        OutputStream out = Files.newOutputStream(outFile.toPath());
                        byte[] buf = new byte[1024];

                        int len;
                        while((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException var10) {
                    getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
                }
            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }
    public void printAsciiArt() {
        getLogger().info("\033[37m+-------------------------------------------------------------------+");
        getLogger().info("\033[37m|\033[36m    ____       __  __            _   __      __  _                 \033[37m|");
        getLogger().info("\033[37m|\033[36m   / __ )___  / /_/ /____  _____/ | / /___ _/ /_(_)___  ____  _____\033[37m|");
        getLogger().info("\033[37m|\033[36m  / __  / _ \\/ __/ __/ _ \\/ ___/  |/ / __ `/ __/ / __ \\/ __ \\/ ___/\033[37m|");
        getLogger().info("\033[37m|\033[36m / /_/ /  __/ /_/ /_/  __/ /  / /|  / /_/ / /_/ / /_/ / / / (__  ) \033[37m|");
        getLogger().info("\033[37m|\033[36m/_____/\\___/\\__/\\__/\\___/_/  /_/ |_/\\__,_/\\__/_/\\____/_/ /_/____/  \033[37m|");
        getLogger().info("\033[37m|\033[36m                                                                   \033[37m|");
        getLogger().info("\033[37m+-------------------------------------------------\033[34mBy Jason31416\033[37m-----+");
    }
    public void loadGUIs(File guiFolder) {
        if (!guiFolder.exists()) {
            guiFolder.mkdirs();
        }else for(File file: guiFolder.listFiles()){
            if(file.isFile()){
                if(file.getName().endsWith(".yml")){
                    getLogger().info("\033[36m- Loading "+file.getName()+":");
                    GUILoader.loadFile(file);
                }
            }else{
                loadGUIs(file);
            }
        }
    }
    // Loading the plugin
    public void registerDataLists() {
        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        storage = new YamlStorage(dataFolder);
        storage.registerDataList(new DataList<AbstractStructure>(){ // Structure
            public String getName(){return "structures";}
            public List<AbstractStructure> getAllData(){
                return new ArrayList<>(AbstractStructure.structures.values());
            }
            public boolean serialize(Object data, IDataItem dataItem){
                if(data instanceof AbstractStructure structure) return AbstractStructure.pack(dataItem, structure);
                return false;
            }
            public AbstractStructure deserialize(IDataItem dataItem){
                return AbstractStructure.unpack(dataItem);
            }
        });
        storage.load();

        UpdateCycle.registerTask("BetterNations.PeriodicSave", new UpdateTask(20, () -> storage.save()));
    }
    public void loadGUIs(){
        File guiFolder = new File(getDataFolder(), "gui");
        getLogger().info("\033[34mLoading GUIs:\033[0m");
        loadGUIs(guiFolder);
    }
    public void loadHooks(){
        // todo
    }
    public void loadCommands(){
        new BetterNationsCommand().register();
    }
    // Unloading the plugin

    // Plugin startup/shutdown logic
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        printAsciiArt();
        PlanetLib.initialize(this);
        saveAllResources();
        AbstractStructure.registerAllStructures();
        registerDataLists();
        loadGUIs();
        Hologram.checkHolograms();
        loadCommands();
        new BukkitRunnable() {
            public void run() {
                loadHooks();
            }
        }.runTaskLater(this, 1);
    }
    @Override
    public void onDisable() {
        for(Hologram i: new ArrayList<>(Hologram.holograms.values())){
            i.removeHologram();
        }
        PlanetLib.shutdown();
    }
}
