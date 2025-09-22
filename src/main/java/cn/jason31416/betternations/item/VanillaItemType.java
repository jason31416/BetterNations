package cn.jason31416.betternations.item;

import org.bukkit.Material;

public class VanillaItemType extends ItemType {
    public final Material material;
    public VanillaItemType(Material material){
        this.material = material;
    }
    public boolean equals(Object other){
        if(other instanceof VanillaItemType){
            return ((VanillaItemType) other).material==material;
        }
        return false;
    }
    public String getName(){
        return material.name();
    }
    public Material getMaterial(){return material;}
}
