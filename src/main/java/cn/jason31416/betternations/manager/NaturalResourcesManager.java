package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleWorld;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NaturalResourcesManager {
    public static Map<SimpleChunkLocation, ItemType> naturalResourcesMap=new HashMap<>();
    public static void load(){
        naturalResourcesMap.clear();
        if(!new File("naturalresources.yml").exists()){
            return;
        }
        YamlConfiguration nrconf = new YamlConfiguration();
        try {
            nrconf.load(new File("naturalresources.yml"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        for (String key : nrconf.getKeys(false)) {
            try {
                String[] chunkLocationStr = key.split(",");
                SimpleChunkLocation chunkLocation = SimpleChunkLocation.of(Integer.parseInt(chunkLocationStr[1]), Integer.parseInt(chunkLocationStr[2]), SimpleWorld.of(chunkLocationStr[0]));
                ItemType resourceType = ItemType.getItemType(nrconf.getString(key, ""));
                naturalResourcesMap.put(chunkLocation, resourceType);
            }catch (Exception e){
                BetterNations.instance.getLogger().severe("Error loading natural resources for chunk "+key+"!");
                e.printStackTrace();
            }
        }
    }
    public static void save(){
        YamlConfiguration nrconf = new YamlConfiguration();
        for(SimpleChunkLocation chunkLocation: naturalResourcesMap.keySet()) {
            String key = chunkLocation.world().getName()+","+chunkLocation.x()+","+chunkLocation.z();
            nrconf.set(key, naturalResourcesMap.get(chunkLocation).getName());
        }
        try {
            nrconf.save(new File("naturalresources.yml"));
        } catch (IOException e) {
            BetterNations.instance.getLogger().severe("Error saving natural resources!");
            e.printStackTrace();
        }
    }
}
