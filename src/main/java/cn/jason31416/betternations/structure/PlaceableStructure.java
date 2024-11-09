package cn.jason31416.betternations.structure;

import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public abstract class PlaceableStructure extends AbstractStructure {
    public static Map<String, Class<?> > placeableStructures=new HashMap<>();
    public ItemType itemType;
    public PlaceableStructure(Material material){
        super(material);
    }
    public PlaceableStructure(Material material, ItemType type, SimpleLocation location) {
        super(material, location);
        itemType = type;
    }
    public static void registerClass(String id, Class<? extends PlaceableStructure> clazz){
        placeableStructures.put(id, clazz);
        AbstractStructure.registerStructureType(clazz);
    }
    public void breakStructure(){
        super.breakStructure();
        if(location.getBukkitLocation().getWorld()==null) return;
        location.getBukkitLocation().getWorld().dropItem(location.getBukkitLocation(), itemType.getItemStack());
    }

    public static void registerAll(){

    }
}
