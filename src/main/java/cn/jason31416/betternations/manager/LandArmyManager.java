package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.ArmorType;
import cn.jason31416.betternations.army.ArmyType;
import cn.jason31416.betternations.structure.PlaceableStructure;
import cn.jason31416.betternations.structure.types.UnitProductionStructure;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.InvalidConfigurationException;
import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.betternations.item.ItemType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LandArmyManager {
    public static Map<String, ArmyType> directPlacements = new HashMap<>();
    public static void loadTypes(ConfigurationSection section){
        for(String i: section.getKeys(false)){
            ConfigurationSection typeSection = section.getConfigurationSection(i);
            if(typeSection==null) continue;
            ArmyType at = new ArmyType(i, typeSection.getString("name"), ArmorType.valueOf(typeSection.getString("armor", "UNARMED").toUpperCase()), Material.getMaterial(typeSection.getString("icon", "STONE").toUpperCase()));
            at.health = typeSection.getDouble("hp") * Config.getDouble("multiplier.army-hp", 1.0);
            at.maxSupply = typeSection.getDouble("supply") * Config.getDouble("multiplier.army-max-supply", 1.0);
            at.consumption = typeSection.getDouble("consume") * Config.getDouble("multiplier.army-supply-consumption", 1.0);
            at.type = typeSection.getString("type");
            ConfigurationSection damages = typeSection.getConfigurationSection("damage");
            if(damages==null) continue;
            for(String j: damages.getKeys(false)){
                at.attack.put(ArmorType.valueOf(j.toUpperCase()), damages.getDouble(j) * Config.getDouble("multiplier.army-damage", 1.0));
            }
            at.register();
        }
    }
    public static int loadProductions(ConfigurationSection section) {
        int cnt = 0;
        for(String i: section.getKeys(false)){
            ConfigurationSection prodSection = section.getConfigurationSection(i+".recipes");
            if (prodSection == null) continue;
            UnitProductionStructure.materialMap.put(i, Objects.requireNonNull(Material.getMaterial(section.getString(i+".material").toUpperCase())));
            PlaceableStructure.registerClass(i, UnitProductionStructure.class);
            Map<String, UnitProductionStructure.Recipe> recipeMap=new HashMap<>();
            for (String j: prodSection.getKeys(false)) {
                if(!ArmyType.armyTypes.containsKey(prodSection.getString(j+".type", "").toLowerCase())){
                    throw new InvalidConfigurationException("army.yml", i);
                }
                recipeMap.put(j, new UnitProductionStructure.Recipe(ArmyType.armyTypes.get(prodSection.getString(j+".type", "").toLowerCase()), (long)(Config.getDouble("multiplier.army-train-speed", 1.0)*prodSection.getLong(j+".time")*1000L)));
                cnt++;
            }
            UnitProductionStructure.recipes.put(i, recipeMap);
        }
        return cnt;
    }
    public static int loadDirectPlacements(ConfigurationSection section){
        int cnt = 0;
        for(String i: section.getKeys(false)){
            if(ItemType.getItemType(i)!=null&&ItemType.getItemType(i).getMaterial()!=Material.AIR){
                if(!ArmyType.armyTypes.containsKey(section.getString(i).toLowerCase())) continue;
                directPlacements.put(i, ArmyType.armyTypes.get(section.getString(i).toLowerCase()));
                cnt++;
            }
        }
        return cnt;
    }
    public static void unregisterAll(){
        ArmyType.armyTypes.clear();
        UnitProductionStructure.recipes.clear();
        directPlacements.clear();
    }
    public static void loadAll(){
        YamlConfiguration file;
        PlanetLib.instance.getLogger().info("\033[34mLoading armies:\033[0m");
        try {
            file = YamlConfiguration.loadConfiguration(new File(BetterNations.instance.getDataFolder(), "army.yml"));
        }catch (Exception e){
            throw new RuntimeException("Unable to load armies for BetterNations!");
        }

        if(file.isConfigurationSection("types")){
            loadTypes(Objects.requireNonNull(file.getConfigurationSection("types")));
            BetterNations.instance.getLogger().info("\033[36m- Loaded "+ Objects.requireNonNull(file.getConfigurationSection("types")).getKeys(false).size()+" types!\033[0m");
        }
        if(file.isConfigurationSection("production")){
            int cnt = loadProductions(Objects.requireNonNull(file.getConfigurationSection("production")));
            BetterNations.instance.getLogger().info("\033[36m- Loaded "+ cnt +" production recipes!\033[0m");
        }
        if(file.isConfigurationSection("direct-placement")){
            int cnt = loadDirectPlacements(Objects.requireNonNull(file.getConfigurationSection("direct-placement")));
            BetterNations.instance.getLogger().info("\033[36m- Loaded "+ cnt +" direct placement recipes!\033[0m");
        }
    }
}
