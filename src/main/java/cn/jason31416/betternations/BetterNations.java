package cn.jason31416.betternations;

import cn.jason31416.betternations.army.BreakCampRunnable;
import cn.jason31416.betternations.army.states.ArmyCamp;
import cn.jason31416.betternations.army.states.ArmyListener;
import cn.jason31416.betternations.army.states.TransportArmy;
import cn.jason31416.betternations.command.BetterNationsCommand;
import cn.jason31416.betternations.command.nation.ToggleArmyUpdateCommand;
import cn.jason31416.betternations.manager.*;
import cn.jason31416.betternations.manager.EventListener;
import cn.jason31416.betternations.manager.map.BlueMapHook;
import cn.jason31416.betternations.manager.map.MapDisplayManager;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.nation.TownLevel;
import cn.jason31416.betternations.nation.TownRole;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.betternations.structure.Hologram;
import cn.jason31416.betternations.structure.PlaceableStructure;
import cn.jason31416.betternations.structure.StructureListener;
import cn.jason31416.betternations.structure.types.Granary;
import cn.jason31416.betternations.structure.types.Machinery;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.Utils;
import cn.jason31416.planetlib.data.DataList;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.data.YamlStorage;
import cn.jason31416.planetlib.gui.GUILoader;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.hook.MythicMobsHook;
import cn.jason31416.planetlib.hook.VaultHook;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.update.UpdateCycle;
import cn.jason31416.planetlib.update.UpdateTask;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class BetterNations extends JavaPlugin {
    public static BetterNations instance;
    public static YamlStorage storage;
    private void saveFolder(String name) throws URISyntaxException, IOException {
        if(new File(getDataFolder(), name).isDirectory()) return;
        URI uri = getClassLoader().getResource(name).toURI();
        try(FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            try(Stream<Path> walk = Files.walk(fileSystem.getPath(name), 1)) {
                for (Iterator<Path> it = walk.iterator(); it.hasNext(); ) {
                    Path i = it.next();
                    if(!i.toString().equals(name)) savePluginResource(i.toString());
                }
            }
        }
    }
    public void saveAllResources() {
        savePluginResource("army.yml");
        try {
            saveFolder("gui/zh_cn");
            saveFolder("gui/en_us");
            saveFolder("item");
            saveFolder("lang");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        savePluginResource("gui/README.md");
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
                    getLogger().info("\033[36m- Loading "+file.getName()+"\033[0m");
                    GUILoader.loadFile(file);
                }
            }else{
                loadGUIs(file);
            }
        }
    }
    public void loadGUIs(){
        File guiFolder = new File(getDataFolder(), "gui/"+Config.getString("lang"));
        getLogger().info("\033[34mLoading GUIs:\033[0m");
        loadGUIs(guiFolder);
    }
    // Loading the plugin
    public void registerDataLists() {
        getLogger().info("\033[34mLoading data...\033[0m");
        File dataFolder = new File(getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        storage = new YamlStorage(dataFolder);
        storage.registerDataList(new DataList<Nation>() { // Nations
            @Override
            public String getName() {
                return "nations";
            }

            @Override
            public List<Nation> getAllData() {
                return new ArrayList<>(Nation.nations.values());
            }

            @Override
            public boolean serialize(Object data, IDataItem dataItem) {
                if(!(data instanceof Nation nation)) return false;
                return nation.serialize(dataItem);
            }

            @Override
            public Nation deserialize(IDataItem dataItem) {
                return Nation.deserialize(dataItem);
            }
        });
        storage.registerDataList(new DataList<Town>() { // Towns
            @Override
            public String getName() {
                return "towns";
            }
            @Override
            public List<Town> getAllData() {
                return new ArrayList<>(Town.towns.values());
            }

            @Override
            public boolean serialize(Object data, IDataItem dataItem) {
                if(!(data instanceof Town town)) return false;
                return town.serialize(dataItem);
            }

            @Override
            public Town deserialize(IDataItem dataItem) {
                return Town.deserialize(dataItem);
            }
        });
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
        storage.registerDataList(new DataList<HistoricalBroadcastManager.HistoricalEvent>(){ // History
            public String getName(){return "history";}
            public List<HistoricalBroadcastManager.HistoricalEvent> getAllData(){
                ArrayList<HistoricalBroadcastManager.HistoricalEvent> ret = new ArrayList<>();
                for(var i: HistoricalBroadcastManager.history.values()) ret.addAll(i);
                return ret;
            }
            public boolean serialize(Object data, IDataItem dataItem){
                if(data instanceof HistoricalBroadcastManager.HistoricalEvent event) return event.serialize(dataItem);
                return false;
            }
            public HistoricalBroadcastManager.HistoricalEvent deserialize(IDataItem dataItem){
                return HistoricalBroadcastManager.HistoricalEvent.deserialize(dataItem);
            }
        });
        storage.load();

        UpdateCycle.registerTask("BetterNations.PeriodicSave", new UpdateTask(60*20, () -> storage.save()));
    }
    // Unloading the plugin

    // Plugin startup/shutdown logic
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        saveAllResources();
        printAsciiArt();
        PlanetLib.initialize(this, "1"); // this is the data's version
        BlueMapHook.init();
        ItemCraftingManager.loadAll();
        LandArmyManager.loadAll();
        PlaceableStructure.registerAll();
        AbstractStructure.registerAllStructures();
        registerDataLists();
        loadGUIs();
        MapDisplayManager.init();
        Granary.loadSupplyWorth();
        ToggleArmyUpdateCommand.bossBar=Bukkit.createBossBar(Message.getMessage("combat.next-update-bossbar").toString(), BarColor.RED, BarStyle.SOLID);
        ToggleArmyUpdateCommand.bossBar.setVisible(true);
        TownLevel.loadLevels();
        NaturalResourcesManager.load();

        UpdateCycle.registerTask("BetterNations.BorderDisplay", new UpdateTask(Config.getInt("border-display.interval"), new BorderDisplayManager()));
        UpdateCycle.registerTask("BetterNations.ArmyUpdate", new UpdateTask(Config.getInt("combat.army-tick-interval")*20, new ArmyUpdateManager()));
        if(Config.getBoolean("combat.enable-animation")) UpdateCycle.registerTask("BetterNations.FromToParticlesUpdate", new UpdateTask(Config.getInt("combat.particle-interval"), FromToAnimationManager::updateAll));
        UpdateCycle.registerTask("BetterNations.ArmyUpdateBossbar", new UpdateTask(5, () -> {
            ToggleArmyUpdateCommand.bossBar.setProgress(Math.min(1, Math.max(0, (ArmyUpdateManager.nextUpdate-System.currentTimeMillis())/1000.0/Config.getInt("combat.army-tick-interval"))));
            ToggleArmyUpdateCommand.bossBar.setTitle(Message.getMessage("combat.next-update-bossbar").add("timer", Utils.formatSeconds((int)(ArmyUpdateManager.nextUpdate-System.currentTimeMillis())/1000)).toString());
        }));
        UpdateCycle.registerTask("BetterNations.MapUpdate", new UpdateTask(Config.getInt("bluemap.check-interval"), MapDisplayManager::update));
        ArmyUpdateManager.nextUpdate = System.currentTimeMillis()+1000L*Config.getInt("combat.army-tick-interval");
        UpdateCycle.registerTask("BetterNations.ClaimingActionbar", new UpdateTask(20, ()->{
            for(SimplePlayer i: new ArrayList<>(EventListener.autoClaiming.keySet())){
                if(!i.isOnline()) EventListener.autoClaiming.remove(i);
                else{
                    switch (EventListener.autoClaiming.get(i)){
                        case CLAIM -> Message.getMessage("auto-claiming.claim").sendActionbar(i);
                        case UNCLAIM -> Message.getMessage("auto-claiming.unclaim").sendActionbar(i);
                        case TOWN_CLAIM -> Message.getMessage("auto-claiming.town-claim").sendActionbar(i);
                        case TOWN_UNCLAIM -> Message.getMessage("auto-claiming.town-unclaim").sendActionbar(i);
                    }
                }
            }
        }));
        UpdateCycle.registerTask("BetterNations.TownDevPointsIncrement", new UpdateTask(20, ()->{
            for(Player i: Bukkit.getOnlinePlayers()){
                SimplePlayer player = SimplePlayer.of(i);
                SimpleChunkLocation chunk = player.getLocation().getChunkLocation();
                if(chunk.isTownChunk()&&Objects.requireNonNull(chunk.getTown()).getRole(player)!=TownRole.NONE&&chunk.getTown().devadded.getOrDefault(player, 0)<Config.getInt("town.dev-points.max-day-exist")){
                    chunk.getTown().devPoints += Config.getDouble("town.dev-points.exists");
                    chunk.getTown().devadded.put(player, chunk.getTown().devadded.getOrDefault(player, 0)+1);
                }
            }
        }));

        UpdateCycle.registerTask("BetterNations.DayChange", new UpdateTask(86400*20, ()->{
            Message.getMessage("town.day-change").broadcast();
            for(Town i: Town.towns.values()){
                i.devadded.clear();
                i.devPoints = Math.max(i.devPoints-Config.getDouble("town.dev-points.drop-per-day"), 0);
            }
        }));

        UpdateCycle.registerTask("BetterNations.MachineryUpdate", new UpdateTask(20, Machinery::updateMachineries));

        new BetterNationsCommand().register();
        Bukkit.getPluginManager().registerEvents(new EventListener(), this);
        Bukkit.getPluginManager().registerEvents(new StructureListener(), this);
        Bukkit.getPluginManager().registerEvents(new ArmyListener(), this);
        Bukkit.getPluginManager().registerEvents(new BreakCampRunnable.CampBreakingListener(), this);
    }
    public void reload(){
        saveAllResources();
        storage.save();
        PlanetLib.reload(this);
        ItemCraftingManager.unregisterAll();
        LandArmyManager.unregisterAll();
        ItemCraftingManager.loadAll();
        LandArmyManager.loadAll();
        Granary.loadSupplyWorth();
        TownLevel.loadLevels();
        NaturalResourcesManager.load();

        GUILoader.loadedGUIs.clear();
        loadGUIs();

        UpdateCycle.unregisterTask("BetterNations.BorderDisplay");
        UpdateCycle.registerTask("BetterNations.BorderDisplay", new UpdateTask(Config.getInt("border-display.interval"), new BorderDisplayManager()));

        UpdateCycle.unregisterTask("BetterNations.FromToParticlesUpdate");
        if(Config.getBoolean("combat.enable-animation")) UpdateCycle.registerTask("BetterNations.FromToParticlesUpdate", new UpdateTask(Config.getInt("combat.particle-interval"), FromToAnimationManager::updateAll));

        UpdateCycle.unregisterTask("BetterNations.MapUpdate");
        MapDisplayManager.reload();
        UpdateCycle.registerTask("BetterNations.MapUpdate", new UpdateTask(Config.getInt("bluemap.check-interval"), MapDisplayManager::update));

        UpdateCycle.unregisterTask("BetterNations.ArmyUpdate");
        UpdateCycle.registerTask("BetterNations.ArmyUpdate", new UpdateTask(Config.getInt("combat.army-tick-interval")*20, new ArmyUpdateManager()));
        ArmyUpdateManager.nextUpdate = System.currentTimeMillis()+1000L*Config.getInt("combat.army-tick-interval");

        for(AbstractStructure i: AbstractStructure.structures.values()){
            i.updateHologram();
        }
    }
    @Override
    public void onDisable() {
        List<Throwable> throwables=new ArrayList<>();
        for(TransportArmy i: new ArrayList<>(TransportArmy.transportArmyMap.values())){
            try {
                SimpleLocation loc = i.mob.getLocation().getBlockLocation();
                while (loc.y() < loc.world().getBukkitWorld().getMaxHeight() && loc.getBlockMaterial() != Material.AIR) {
                    loc = loc.getRelative(0, 1, 0);
                }
                if (loc.y() >= loc.world().getBukkitWorld().getMaxHeight()) continue;
                ArmyCamp c = new ArmyCamp();
                i.unregister();
                c.stack = i.stack;
                c.location = loc;
                i.stack.curHolder = c;
                c.place();
                i.mob.remove();
            }catch (Exception e){
                throwables.add(e);
            }
        }
        for(GUISession i: new ArrayList<>(GUISession.sessions.values())){
            i.close();
        }
        if(ToggleArmyUpdateCommand.bossBar!=null) ToggleArmyUpdateCommand.bossBar.removeAll();
        MapDisplayManager.unload();
        for(BreakCampRunnable i: BreakCampRunnable.breakingPlayers.values()){
            try {
                i.failed();
            }catch (Exception e){
                throwables.add(e);
            }
        }
        for(Collection<Hologram> i: new ArrayList<>(Hologram.holograms.values())){
            for(Hologram j: i){
                try {
                    j.despawn();
                }catch (Exception e){
                    throwables.add(e);
                }
            }
        }
        try {
            storage.save();
        }catch (Exception e){
            throwables.add(e);
        }
        PlanetLib.shutdown();
        if(!throwables.isEmpty()){
            Bukkit.getLogger().severe("Encountered "+throwables.size()+" errors while attempting to shut down BetterNations!");
            Bukkit.getLogger().severe("Below is one of them:");
            throwables.get(0).printStackTrace();
        }
    }
}
