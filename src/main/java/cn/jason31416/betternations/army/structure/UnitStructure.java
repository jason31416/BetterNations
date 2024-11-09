package cn.jason31416.betternations.army.structure;


import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;

import java.util.UUID;

public abstract class UnitStructure extends AbstractStructure { // The block state of the army
    public UnitStructure(Material material) {
        super(material);
    }

    public UnitStructure(Material material, SimpleLocation location) {
        super(material, location);
    }
    public ArmyStack stack;
    @Override
    public boolean serialize(IDataItem dataItem) {

        return true;
    }

    @Override
    public void deserialize(IDataItem dataItem) {
    }
}