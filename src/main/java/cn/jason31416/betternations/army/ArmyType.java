package cn.jason31416.betternations.army;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class ArmyType {
    public static Map<String, ArmyType> armyTypes = new HashMap<>();
    public String id, name, type;
    public double health, maxSupply, consumption;
    public Material icon;
    public Map<ArmorType, Double> attack=new HashMap<>();
    public ArmorType armorType;
    public ArmyType(String id, String name, ArmorType armorType, Material icon) {
        this.id = id;
        this.name = name;
        this.armorType = armorType;
        this.icon = icon;
    }
    public ArmyType register(){
        armyTypes.put(id, this);
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ArmyType tp){
            return tp.id.equals(id);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
