package cn.jason31416.betternations.army.states;

import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.betternations.army.BreakCampRunnable;
import cn.jason31416.betternations.manager.FromToAnimationManager;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.betternations.structure.types.Granary;
import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Color;
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
        for(StructuredArmy i: armyLocationMap.get(location.getChunkLocation())){
            if(i.running&&i.getStack().nation.getRelation(stack.nation)== Relation.ENEMY){
                new FromToAnimationManager(this, i, Color.RED);
                new FromToAnimationManager(i, this, Color.RED);
            }
        }
        if(stack.nation.getRelation(location.getChunkLocation().getNation())==Relation.ALLY&&
                Granary.granaries.containsKey(location.getChunkLocation()))
            for(Granary i: Granary.granaries.get(location.getChunkLocation())){
                new FromToAnimationManager(i, this, Color.YELLOW);
            }
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

    public boolean isInCombat(){
        for(StructuredArmy i: armyLocationMap.get(location.getChunkLocation())){
            if(i.running&&i.getStack().nation.getRelation(stack.nation) == Relation.ENEMY){
                return true;
            }
        }
        return false;
    }

    @Override
    public ArmyStack getStack() {
        return stack;
    }

    @Override
    public boolean serialize(IDataItem dataItem) {
        if(!stack.nation.exists()) return false;
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

    public static boolean isInvasionChunk(SimpleChunkLocation chunk){
        if(!armyLocationMap.containsKey(chunk)) return false;
        for(StructuredArmy i: armyLocationMap.get(chunk)) {
            if(i instanceof InvasionFlag || i instanceof SiegeFlag){
                return true;
            }
        }
        return false;
    }
}
