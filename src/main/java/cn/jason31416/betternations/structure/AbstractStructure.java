package cn.jason31416.betternations.structure;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.states.ArmyCamp;
import cn.jason31416.betternations.army.states.InvasionFlag;
import cn.jason31416.betternations.army.states.SiegeFlag;
import cn.jason31416.betternations.structure.types.TownCore;
import cn.jason31416.betternations.structure.types.TownRuin;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractStructure {
    public enum InteractionType {
        NONE,
        INTERACT,
        BREAK,
        SNEAK_CLICK
    }
    public static final Map<SimpleLocation, AbstractStructure> structures = new HashMap<>();
    public static final Map<String, Class<? extends AbstractStructure> > structureTypes = new HashMap<>();
    public SimpleLocation location;
    public UUID uuid=UUID.randomUUID();
    public boolean exists=false;
    public abstract Material getMaterial();
    public Hologram hologram=null;
    public abstract boolean serialize(IDataItem dataItem);
    public abstract void deserialize(IDataItem dataItem);
    public String getHologramText(){
        return Message.getMessage("structure."+getID()+".hologram").toString();
    }
    public String getID(){
        return getClass().getSimpleName().toLowerCase(Locale.ROOT);
    }
    public void place(){
        location.setBlockMaterial(getMaterial());
        hologram = Hologram.createHologram(SimpleLocation.of(location.getBlock().getLocation().add(0.5, 1.3, 0.5)), getHologramText());
        register();
        exists=true;
    }
    public void register() {
        structures.put(location, this);
    }
    public void unregister() {
        structures.remove(location);
    }
    public static boolean pack(IDataItem dataItem, AbstractStructure structure){
        String structureType = structure.getID();
        boolean success = structure.serialize(dataItem);
        dataItem.put("structureType", structureType);
        dataItem.put("location", structure.location.x()+"_"+structure.location.y()+"_"+structure.location.z()+"_"+structure.location.world().getBukkitWorld().getUID().toString());
        dataItem.setUUID(structure.uuid);
        return success;
    }
    public void updateHologram(){
        hologram.setText(getHologramText());
    }
    public void breakStructure(){
        if(Bukkit.isPrimaryThread()) {
            hologram.removeHologram();
            location.setBlockMaterial(Material.AIR);
        }else{
            new BukkitRunnable() {
                public void run(){
                    hologram.removeHologram();
                    location.setBlockMaterial(Material.AIR);
                }
            }.runTaskLater(BetterNations.instance, 0);
        }
        exists = false;
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
        structure.location = new SimpleLocation(Double.parseDouble(locationStr[0]), Double.parseDouble(locationStr[1]), Double.parseDouble(locationStr[2]), SimpleWorld.of(UUID.fromString(locationStr[3]))).getBlockLocation();
        structure.deserialize(dataItem);
        structure.place();
        return structure;
    }
    public abstract boolean processInteraction(InteractionType type, SimplePlayer player);
    public static void registerStructureType(Class<? extends AbstractStructure> structureClass) {
        structureTypes.put(structureClass.getSimpleName().toLowerCase(), structureClass);
    }
    public static void registerAllStructures() {
        registerStructureType(TownCore.class);
        registerStructureType(ArmyCamp.class);
        registerStructureType(InvasionFlag.class);
        registerStructureType(SiegeFlag.class);
        registerStructureType(TownRuin.class);
    }
}
