package cn.jason31416.betternations.structure;

import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractStructure {
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
        this.location = location;
        this.uuid = UUID.randomUUID();
        this.material = material;
    }
    public abstract void onInteract();
    public abstract boolean serialize(IDataItem dataItem);
    public abstract void deserialize(IDataItem dataItem);

    public String getHologramText(){
        return ChatColor.translateAlternateColorCodes('&', Config.getString("structure.hologram").replace("%name%", Config.getString("structure." + getClass().getSimpleName().toLowerCase() + ".name")));
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
        dataItem.put("location", structure.location.serialize());
        dataItem.setUUID(structure.uuid);
        return success;
    }
    public void breakStructure(){
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
        structure.deserialize(dataItem);
        return structure;
    }
    public static void registerStructureType(Class<? extends AbstractStructure> structureClass) {
        structureTypes.put(structureClass.getSimpleName(), structureClass);
    }
    public static void registerAllStructures() {
        registerStructureType(TownCore.class);
    }
}
