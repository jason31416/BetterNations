package cn.jason31416.betternations.structure.types;

import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.structure.PlaceableStructure;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;

import java.util.HashSet;
import java.util.Set;

public class Extractor extends PlaceableStructure {
    public static Set<SimpleChunkLocation> extractors = new HashSet<>();

    @Override
    public void register(){
        super.register();
        extractors.add(location.getChunkLocation());
    }
    @Override
    public void unregister(){
        super.unregister();
        extractors.remove(location.getChunkLocation());
    }
    @Override
    public String getHologramText(){
        return Message.getMessage("structure.extractor.hologram").toString();
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
        return player.hasPermission(Permission.STRUCTURE, location);
    }
}
