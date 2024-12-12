package cn.jason31416.betternations.structure.types;


import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.states.StructuredArmy;
import cn.jason31416.betternations.manager.FromToAnimationManager;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.structure.PlaceableStructure;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Granary extends PlaceableStructure {
    public static Map<SimpleChunkLocation, Set<Granary> > granaries = new HashMap<>();
    public static Map<ItemType, Double> supplyWorth = new HashMap<>();
    public static void loadSupplyWorth(){
        ConfigurationSection section = BetterNations.instance.getConfig().getConfigurationSection("supply");
        if(section==null) return;
        for(String i: section.getKeys(false)){
            try {
                ItemType tp = ItemType.getItemType(i.toUpperCase());
                supplyWorth.put(tp, section.getDouble(i));
            }catch (RuntimeException e){
                Bukkit.getLogger().warning(i+" in BetterNations config is not a valid item!");
            }
        }
    }
    public double supply=0;
    @Override
    public void register(){
        super.register();
        if(!granaries.containsKey(location.getChunkLocation())) granaries.put(location.getChunkLocation(), new HashSet<>());
        granaries.get(location.getChunkLocation()).add(this);
        if(StructuredArmy.armyLocationMap.containsKey(location.getChunkLocation()))
            for(StructuredArmy i: StructuredArmy.armyLocationMap.get(location.getChunkLocation())) {
                if(i.stack.nation.getRelation(location.getChunkLocation().getNation()) == Relation.ALLY){
                    new FromToAnimationManager(this, i, Color.YELLOW);
                }
            }
    }
    @Override
    public void unregister(){
        super.unregister();
        if(granaries.containsKey(location.getChunkLocation())) {
            granaries.get(location.getChunkLocation()).remove(this);
            if(granaries.get(location.getChunkLocation()).isEmpty()) granaries.remove(location.getChunkLocation());
        }
    }
    @Override
    public Material getMaterial() {
        return Material.HAY_BLOCK;
    }
    @Override
    public String getHologramText(){
        return Message.getMessage("structure."+getID()+".hologram").add("supply", Math.round(supply*100.0)/100.0).add("max_supply", Config.getDouble("combat.granary-supply-limit")).toString();
    }

    @Override
    public boolean serialize(IDataItem dataItem) {
        dataItem.set("supply", supply);
        return true;
    }

    @Override
    public void deserialize(IDataItem dataItem) {
        supply = dataItem.getDouble("supply");
    }

    @Override
    public boolean processInteraction(InteractionType type, SimplePlayer player) {
        if(!player.hasPermission(Permission.STRUCTURE, location)) return false;
        if(type == InteractionType.INTERACT){
            if(!location.getChunkLocation().isTownChunk()) return false;
            ItemType mainHandItem = ItemType.getItemType(player.getPlayer().getInventory().getItemInMainHand());
            if(supplyWorth.containsKey(mainHandItem)){
                supply += supplyWorth.get(mainHandItem);
                player.getPlayer().getInventory().getItemInMainHand().setAmount(Math.max(0, player.getPlayer().getInventory().getItemInMainHand().getAmount()-1));
                updateHologram();
            }else{
                Message.getMessage("combat.item-not-edible").send(player);
            }
        }
        return true;
    }
}
