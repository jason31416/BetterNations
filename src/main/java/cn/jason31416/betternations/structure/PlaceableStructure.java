package cn.jason31416.betternations.structure;

import cn.jason31416.betternations.structure.types.UnitProductionStructure;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class PlaceableStructure extends AbstractStructure {
    public static Map<String, Class<? extends PlaceableStructure> > placeableStructures=new HashMap<>();
    public static Map<Class<? extends PlaceableStructure>, ItemType > structureMap=new HashMap<>();
    public static void registerClass(String id, Class<? extends PlaceableStructure> clazz){
        placeableStructures.put(id, clazz);
        structureMap.put(clazz, ItemType.getItemType(id));
        AbstractStructure.registerStructureType(clazz);
    }
    public void breakStructure() {
        super.breakStructure();
        if(location.getBukkitLocation().getWorld()==null) return;
        location.getBukkitLocation().getWorld().dropItem(location.getBukkitLocation(), structureMap.get(getClass()).getItemStack());
    }

    public static void registerAll() {
    }
}
