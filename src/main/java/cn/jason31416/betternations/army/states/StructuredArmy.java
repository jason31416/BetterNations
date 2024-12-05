package cn.jason31416.betternations.army.states;

import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.betternations.army.BreakCampRunnable;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class StructuredArmy extends AbstractStructure implements ArmyStackHolder {
    public static Map<SimpleChunkLocation, Set<StructuredArmy>> armyLocationMap = new HashMap<>();
    public ArmyStack stack;
    public BreakCampRunnable runnable = null;
    public boolean running = true;

    public StructuredArmy(){}
    @Override
    public void register() {
        super.register();
        if(!armyLocationMap.containsKey(location.getChunkLocation())) armyLocationMap.put(location.getChunkLocation(), new HashSet<>());
        armyLocationMap.get(location.getChunkLocation()).add(this);
        stack.curHolder = this;
    }
    @Override
    public void unregister(){
        super.unregister();
        running = false;
        if(armyLocationMap.containsKey(location.getChunkLocation())){
            armyLocationMap.get(location.getChunkLocation()).remove(this);
            if(armyLocationMap.get(location.getChunkLocation()).isEmpty()) armyLocationMap.remove(location.getChunkLocation());
        }
    }

    @Override
    public ArmyStack getStack() {
        return stack;
    }

    @Override
    public void update() {
        // todo: update camps
    }

    @Override
    public boolean serialize(IDataItem dataItem) {
        stack.serialize(dataItem);
        return true;
    }

    @Override
    public void deserialize(IDataItem dataItem) {
        stack = ArmyStack.deserialize(dataItem);
    }

    @Override
    public SimpleLocation getLocation() {
        return location;
    }
}
