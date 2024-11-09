package cn.jason31416.betternations.structure.types;

import cn.jason31416.betternations.structure.PlaceableStructure;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;

public abstract class UnitProductionStructure extends PlaceableStructure {
    public UnitProductionStructure(Material material){
        super(material);
    } // produces infantry-like units
    public UnitProductionStructure(Material material, ItemType type, SimpleLocation location) {
        super(material, type, location);
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
        return false;
    }
}
