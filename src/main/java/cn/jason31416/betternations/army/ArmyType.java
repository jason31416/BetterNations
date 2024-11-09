package cn.jason31416.betternations.army;

import java.util.HashMap;
import java.util.Map;

public class ArmyType {
    public static Map<String, ArmyType> armyTypes = new HashMap<>();
    public String id, name;
    public double health;
    public Map<ArmorType, Double> attack;
    public ArmorType armorType;
    public ArmyType(String id, String name, ArmorType armorType) {
        this.id = id;
        this.name = name;
        this.armorType = armorType;
    }
    public void register(){
        armyTypes.put(id, this);
    }
}
