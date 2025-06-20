package cn.jason31416.betternations.nation;

import cn.jason31416.planetlib.Config;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class TownLevel {
    public static List<TownLevel> townLevels=new ArrayList<>();
    public String name;
    public double points;
    public Material material;
    public TownLevel(String displayName, double actpoints, Material mat){
        name=displayName;
        points=actpoints;
        material=mat;
        bb:{
            for (int i = 0; i < townLevels.size(); i++) {
                if (townLevels.get(i).points>actpoints){
                    townLevels.add(i, this);
                    break bb;
                }
            }
            townLevels.add(this);
        }
    }
    public static void loadLevels(){
        townLevels.clear();
        for(String i: Config.getKeys("town.levels")){
            new TownLevel(Config.getString("town.levels."+i+".name"),
                    Config.getDouble("town.levels."+i+".points"),
                    Material.getMaterial(Config.getString("town.levels."+i+".material").toUpperCase()));
        }
    }
    public TownLevel getNextLevel(){
        if(townLevels.indexOf(this)+1>=townLevels.size()){
            return null;
        }
        return townLevels.get(townLevels.indexOf(this)+1);
    }
}
