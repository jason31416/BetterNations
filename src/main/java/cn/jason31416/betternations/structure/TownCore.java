package cn.jason31416.betternations.structure;

import cn.jason31416.betternations.nation.Town;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class TownCore extends AbstractStructure {
    Town town;
    public TownCore(){
        super(Material.BEACON);
    }
    public TownCore(SimpleLocation location, Town town) {
        super(Material.BEACON, location);
        place();
        this.town = town;
    }
    @Override
    public void onInteract() {
    }
    @Override
    public boolean serialize(IDataItem dataItem) {
        dataItem.set("town", town.getId());
        return true;
    }

    @Override
    public void deserialize(IDataItem dataItem) {
        town = Town.getTown(UUID.fromString(dataItem.getString("town")));
    }
}
