package cn.jason31416.betternations.structure;

import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleWorld;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractStructure {
    public enum InteractionType {
        NONE,
        INTERACT,
        BREAK
    }
    public static final Map<SimpleLocation, AbstractStructure> structures = new HashMap<>();
    public static final Map<String, Class<? extends AbstractStructure> > structureTypes = new HashMap<>();
    public SimpleLocation location;
    public UUID uuid;
    final Material material;
    Hologram hologram=null;
    public AbstractStructure(Material material){
        this.material = material;
    }
    public AbstractStructure(Material material, SimpleLocation location) {
        this.location = location.getBlockLocation();
        this.uuid = UUID.randomUUID();
        this.material = material;
    }
    public abstract boolean serialize(IDataItem dataItem);
    public abstract void deserialize(IDataItem dataItem);

    public String getHologramText(){
        return Message.getMessage("structure."+getClass().getSimpleName().toLowerCase()+".hologram").toString();
    }
    public void place(){
        location.setBlockMaterial(material);
        hologram = Hologram.createHologram(SimpleLocation.of(location.getBlock().getLocation().add(0.5, 1.3, 0.5)), getHologramText());
        register();
    }
    public void register() {
        structures.put(location, this);
    }
    public void unregister() {
        structures.remove(location);
    }
    public static boolean pack(IDataItem dataItem, AbstractStructure structure){
        String structureType = structure.getClass().getSimpleName();
        boolean success = structure.serialize(dataItem);
        dataItem.put("structureType", structureType);
        dataItem.put("location", structure.location.x()+"_"+structure.location.y()+"_"+structure.location.z()+"_"+structure.location.world().getBukkitWorld().getUID().toString());
        dataItem.setUUID(structure.uuid);
        return success;
    }
    public void breakStructure(){
        hologram.removeHologram();
        location.setBlockMaterial(Material.AIR);
    }
    public static AbstractStructure unpack(IDataItem dataItem) {
        String structureType = dataItem.getString("structureType");
        Class<? extends AbstractStructure> structureClass = structureTypes.get(structureType);
        if (structureClass == null) {
            throw new IllegalArgumentException("Unknown structure type: " + structureType);
        }
        AbstractStructure structure;
        try {
            structure = structureClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        structure.uuid = dataItem.getUUID();
        String[] locationStr = dataItem.getString("location").split("_");
        structure.location = new SimpleLocation(Double.parseDouble(locationStr[0]), Double.parseDouble(locationStr[1]), Double.parseDouble(locationStr[2]), SimpleWorld.of(UUID.fromString(locationStr[3])));
        structure.deserialize(dataItem);
        structure.place();
        return structure;
    }
    public abstract boolean processInteraction(InteractionType type, SimplePlayer player);
    public static void registerStructureType(Class<? extends AbstractStructure> structureClass) {
        structureTypes.put(structureClass.getSimpleName(), structureClass);
    }
    public static void registerAllStructures() {
        registerStructureType(TownCore.class);
    }
}
