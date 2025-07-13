package cn.jason31416.betternations.structure.types;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.manager.NaturalResourcesManager;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.structure.PlaceableStructure;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.item.CustomItemType;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Extractor extends PlaceableStructure {
    public static Map<SimpleChunkLocation, Extractor> extractors = new HashMap<>();

    public int produced=0;

    @Override
    public void register(){
        super.register();
        extractors.put(location.getChunkLocation(), this);
    }
    @Override
    public void unregister(){
        super.unregister();
        extractors.remove(location.getChunkLocation());
    }
    @Override
    public String getHologramText(){
        return Message.getMessage("structure.extractor.hologram").add("current", produced).toString();
    }
    @Override
    public Material getMaterial() {
        return Material.IRON_BLOCK;
    }

    @Override
    public boolean serialize(IDataItem dataItem) {
        return true;
    }

    @Override
    public void deserialize(IDataItem dataItem) {
    }

    @Override
    public boolean processInteraction(InteractionType type, SimplePlayer player) {
        if(NaturalResourcesManager.naturalResourcesMap.containsKey(location.getChunkLocation()) && type == InteractionType.INTERACT){
            if(player.hasPermission(Permission.STRUCTURE, location)){
                if(produced > 0 && player.getPlayer().getInventory().firstEmpty() >= 0){
                    player.getPlayer().getInventory().addItem(getItemType().getItemStack(produced));
                    produced = 0;
                    updateHologram();
                }
            }
        }
        return player.hasPermission(Permission.STRUCTURE, location);
    }
    public ItemType getItemType(){
        return NaturalResourcesManager.naturalResourcesMap.get(location.getChunkLocation());
    }

    @Override
    public void breakStructure() {
        super.breakStructure();
        if(produced>0){
            location.world().getBukkitWorld().dropItem(location.getBukkitLocation().add(0.5, 0.5, 0.5), getItemType().getItemStack(produced));
        }
    }

    public static void tickAll(){
        for(SimpleChunkLocation location : extractors.keySet()){
            if(NaturalResourcesManager.naturalResourcesMap.containsKey(location)){
                Extractor extractor = extractors.get(location);
                extractor.produced = Math.min(extractor.produced + 1, NaturalResourcesManager.naturalResourcesMap.get(location).getMaterial().getMaxStackSize());
                extractor.updateHologram();
            }
        }
    }
}
