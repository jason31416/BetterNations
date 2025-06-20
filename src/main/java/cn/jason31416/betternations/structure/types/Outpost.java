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

public class Outpost extends PlaceableStructure {
    public static Set<SimpleChunkLocation> outposts = new HashSet<>();
    @Override
    public void register(){
        super.register();
        outposts.add(location.getChunkLocation());
    }
    @Override
    public void unregister(){
        super.unregister();
        outposts.remove(location.getChunkLocation());
    }
    @Override
    public String getHologramText(){
        return Message.getMessage("structure.outpost.hologram").toString();
    }
    @Override
    public Material getMaterial() {
        return Material.BELL;
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
        return player.hasPermission(Permission.STRUCTURE, location);
    }
}

