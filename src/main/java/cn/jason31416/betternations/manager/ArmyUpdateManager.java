package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.ArmorType;
import cn.jason31416.betternations.army.states.ArmyCamp;
import cn.jason31416.betternations.army.states.InvasionFlag;
import cn.jason31416.betternations.army.states.StructuredArmy;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.update.UpdateTask;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ArmyUpdateManager implements UpdateTask.RunnableTask {
    public static Map<SimpleChunkLocation, Double> chunkHealths = new HashMap<>();
    public static long nextUpdate=0;
    @Override
    public void run() {
        for (SimpleChunkLocation chunk: new HashSet<>(StructuredArmy.armyLocationMap.keySet())){
            Set<StructuredArmy> armies = StructuredArmy.armyLocationMap.get(chunk);
            for(StructuredArmy army: armies){
                for(StructuredArmy other: armies){
                    if(other.stack.nation.getRelation(army.stack.nation)==Relation.ENEMY){
                        army.stack.damage(other.stack); // other.stack attack army.stack
                        army.hologram.setText(army.getHologramText());
                    }
                }
            }
            for(StructuredArmy army: new HashSet<>(armies)){
                if(army.stack.size()<=0&&army.runnable==null){
                    army.breakStructure();
                    army.unregister();
                }
            }
            for(StructuredArmy army: new HashSet<>(armies)){
                if(army instanceof InvasionFlag invasion){
                    if(!chunk.isClaimed()||chunk.isTownChunk()||army.stack.nation.getRelation(chunk.getNation())!= Relation.ENEMY){
                        invasion.convertToCamp();
                        continue;
                    }
                    if(!chunkHealths.containsKey(chunk)){
                        chunkHealths.put(chunk, Config.getDouble("combat.chunk-hp", 20));
                    }
                    chunkHealths.put(chunk, chunkHealths.get(chunk)-invasion.stack.getDamageTowards(ArmorType.TERRITORY));
                }
            }
            if(chunkHealths.containsKey(chunk)&&chunkHealths.get(chunk) <= 0){
                new BukkitRunnable(){
                    public void run() {
                        double mxatt = -1;
                        Nation tnation = null;
                        for (StructuredArmy army : new HashSet<>(StructuredArmy.armyLocationMap.get(chunk))) {
                            if (army instanceof InvasionFlag invasion) {
                                invasion.convertToCamp();
                                if (invasion.stack.getDamageTowards(ArmorType.TERRITORY) > mxatt) {
                                    mxatt = invasion.stack.getDamageTowards(ArmorType.TERRITORY);
                                    tnation = invasion.stack.nation;
                                }
                            }
                        }
                        chunkHealths.remove(chunk);
                        if (tnation != null) {
                            chunk.unclaim();
                            tnation.claim(chunk);
                        }
                    }
                }.runTaskLater(BetterNations.instance, 0);
            }
        }
        nextUpdate = System.currentTimeMillis()+1000L*Config.getInt("combat.army-tick-interval");
    }
}
