package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.ArmorType;
import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.betternations.army.ArmyType;
import cn.jason31416.betternations.army.states.*;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.betternations.structure.types.Granary;
import cn.jason31416.betternations.structure.types.Outpost;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.update.UpdateTask;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArmyUpdateManager implements UpdateTask.RunnableTask {
    public static Map<SimpleChunkLocation, Double> chunkHealths = new HashMap<>();
    public static long nextUpdate=0;
    private void checkChunkAfterInvasion(SimpleChunkLocation origchunk, Nation winner, Nation loser){
        if(loser.isBarbarian()) return;
        Set<SimpleChunkLocation> encircled = new HashSet<>();
        outer: for(SimpleChunkLocation adj: origchunk.getAdjacentChunks()) {
            if (adj.getNation() == loser && !encircled.contains(adj)) {
                Queue<SimpleChunkLocation> chunks=new ArrayDeque<>();
                Set<SimpleChunkLocation> searched=new HashSet<>();
                chunks.add(adj);
                searched.add(adj);
                int cnt=0;
                while(!chunks.isEmpty()) {
                    SimpleChunkLocation cur = chunks.poll();
//                    System.out.println("Searching "+cur+","+cnt+"-"+searched.size());
                    if (cur.isTownChunk() || Outpost.outposts.contains(cur)) {
//                        System.out.println("Found town");
                        continue outer;
                    }
                    for (SimpleChunkLocation c : cur.getAdjacentChunks()) {
                        if (c.getNation() == loser && !searched.contains(c)) {
                            chunks.add(c);
                            searched.add(c);
                        }
                    }
                    cnt++;
                }
                encircled.addAll(searched);
            }
        }
        new BukkitRunnable(){
            public void run() {
                for (SimpleChunkLocation i : encircled) {
                    loser.forceUnclaim(i);
                    winner.claim(i);
                }
            }
        }.runTaskLater(BetterNations.instance, 0);
    }
    @Override
    public void run() {
        if(Config.getBoolean("combat.enable-supply-system")) {
            new BukkitRunnable() {
                public void run() {
                    for (TransportArmy army : new HashSet<>(TransportArmy.transportArmyMap.values())) {
                        army.stack.supply -= army.stack.getSupplyConsumption() * 2;
                        if (army.stack.supply <= 0) army.destroy(true);
                    }
                }
            }.runTaskLater(BetterNations.instance, 0);
        }
        for(Town town: Town.towns.values()){
            town.townHealth = Math.min(town.townHealth+Config.getDouble("town.hp-recover-speed"), town.getMaxHealth());
        }
        for (SimpleChunkLocation chunk: new HashSet<>(StructuredArmy.armyLocationMap.keySet())){
            try {
                Set<StructuredArmy> armies = StructuredArmy.armyLocationMap.get(chunk);
                for (StructuredArmy army : armies) {
                    if (Config.getBoolean("combat.enable-supply-system") && !army.stack.nation.isBarbarian()) {
                        army.stack.supply -= army.stack.getSupplyConsumption();
                        if (army.stack.nation.getRelation(chunk.getNation()) == Relation.ALLY &&
                                Granary.granaries.containsKey(chunk)) {
                            for (Granary i : Granary.granaries.get(chunk)) {
                                if (i.supply <= 0) continue;
                                double t = Math.min(army.stack.getMaxSupply() - army.stack.supply, Math.min(Config.getDouble("combat.supply-regain-speed"), i.supply));
                                i.supply -= t;
                                army.stack.supply += t;
                                i.updateHologram();
                            }
                        }
                    }
                    for (StructuredArmy other : armies) {
                        if (other.stack.nation.getRelation(army.stack.nation) == Relation.ENEMY) {
                            army.stack.damage(other.stack); // other.stack attack army.stack
                        }
                    }
                    if(army instanceof SiegeFlag siege) {
                        for(SimpleChunkLocation townChunks: siege.target.getTownChunks()){
                            if(StructuredArmy.armyLocationMap.containsKey(townChunks)){
                                for(StructuredArmy otherArmy: StructuredArmy.armyLocationMap.get(townChunks)){
                                    if(otherArmy.stack.nation.getRelation(siege.stack.nation) == Relation.ENEMY){
                                        otherArmy.stack.damage(siege.stack);
                                        siege.stack.damage(otherArmy.stack);
                                    }
                                }
                            }
                        }
                    }
                }
                for (StructuredArmy army : new HashSet<>(armies)) {
                    army.hologram.setText(army.getHologramText());
                    if (!army.stack.nation.exists() || (army.stack.size() <= 0 || army.stack.supply <= 0) && army.runnable == null) {
                        army.breakStructure();
                        army.unregister();
                    }else if(army.location.getBlockMaterial().isAir()&&army.exists){
                        Bukkit.getScheduler().runTask(BetterNations.instance, () -> {
                            army.location.setBlockMaterial(army.getMaterial());
                        });
                    }
                }
                mostOuter:
                for (StructuredArmy army : new HashSet<>(armies)) {
                    if (army instanceof InvasionFlag invasion) {
                        if (!chunk.isClaimed() || chunk.isTownChunk() || army.stack.nation.getRelation(chunk.getNation()) != Relation.ENEMY) {
                            invasion.convertToCamp();
                            continue;
                        }
                        if (!chunkHealths.containsKey(chunk)) {
                            chunkHealths.put(chunk, Config.getDouble((chunk.getNation().isBarbarian()?"barbarian.barbarian-chunk-hp":"combat.chunk-hp"), 20));
                        }
                        chunkHealths.put(chunk, chunkHealths.get(chunk) - invasion.stack.getDamageTowards(ArmorType.TERRITORY));
                    }else if(army instanceof SiegeFlag siege){
                        new BukkitRunnable() {
                            public void run() {
                                if (siege.target == null ||
                                        siege.stack.nation.getRelation(siege.target.getNation()) != Relation.ENEMY ||
                                        siege.stack.nation.getRelation(siege.getLocation().getChunkLocation().getNation()) != Relation.ALLY) {
                                    siege.convertToCamp();
                                    return;
                                }
                                bb:
                                {
                                    for (SimpleChunkLocation adj : siege.getLocation().getChunkLocation().getAdjacentChunks()) {
                                        if (adj.getTown() == siege.target) break bb;
                                    }
                                    siege.convertToCamp();
                                    return;
                                }
                                siege.target.damage(siege.stack);
                                if (siege.target.townHealth <= 0) {
                                    HistoricalBroadcastManager.broadcast(Message.getMessage("history.town-sieged")
                                            .add("nation", siege.stack.nation.getName()).add("town", siege.target.getName()), List.of(siege.target.getNation(), siege.stack.nation));
                                    siege.target.transferNation(siege.stack.nation);
                                    siege.target.townHealth = siege.target.getMaxHealth()/2.0;
                                    siege.convertToCamp();
                                }
                            }
                        }.runTaskLater(BetterNations.instance, 0);
                    }else if(army instanceof ArmyCamp camp){
                        if(chunk.isTownChunk()){
                            Town town = chunk.getTown();
                            if(town!=null) {
                                town.townHealth = Math.min(town.townHealth + camp.stack.getDamageTowards(ArmorType.TERRITORY)/2.0, town.getMaxHealth());
                            }
                        }
                        if(army.stack.nation.isBarbarian()){
                            List<SimpleChunkLocation> adjs = new ArrayList<>();
                            adjs.add(chunk);
                            adjs.addAll(chunk.getAdjacentChunks());
                            outer:
                            for(SimpleChunkLocation adj: adjs){
                                if(adj.getNation()!=null && !adj.getNation().isBarbarian()){
                                    for(StructuredArmy other: StructuredArmy.armyLocationMap.getOrDefault(adj, new HashSet<>())){
                                        if(other.stack.nation.isBarbarian()){
                                            continue outer;
                                        }
                                    }
                                    camp.breakStructure();
                                    camp.unregister();
                                    BarbarianInvasionManager.startBarbarianInvasionAt(adj, army.stack);
                                    continue mostOuter;
                                }
                            }
                        }
                    }
                    if(army.stack.nation.isBarbarian() && Math.random()<Config.getDouble("barbarian.army.recover-chance", 0.1) && army.stack.size()<Config.getInt("barbarian.army.max-count", 15)){
                        List<String> possibleTypes = new ArrayList<>(Config.config.getConfigurationSection("barbarian.army.units").getKeys(false));
                        int tot = 0;
                        for(String type:possibleTypes){
                            tot += Config.getInt("barbarian.army.units."+type, 0);
                        }
                        int num = new Random().nextInt(tot), cur=0;
                        while(cur<possibleTypes.size()&&num>=Config.getInt("barbarian.army.units."+possibleTypes.get(cur), 0)){
                            num -= Config.getInt("barbarian.army.units."+possibleTypes.get(cur), 0);
                            cur++;
                        }
                        String type = possibleTypes.get(cur);
                        army.stack.addArmy(ArmyType.armyTypes.get(type), 1);
                    }
                }
                if (chunkHealths.containsKey(chunk) && chunkHealths.get(chunk) <= 0) {
                    new BukkitRunnable() {
                        public void run() {
                            double mxatt = -1;
                            Nation tnation = null;
                            for (StructuredArmy army : new HashSet<>(StructuredArmy.armyLocationMap.getOrDefault(chunk, new HashSet<>()))) {
                                if(!army.stack.isAlive()) continue;
                                if (army instanceof InvasionFlag invasion) {
                                    if (invasion.stack.getDamageTowards(ArmorType.TERRITORY) > mxatt) {
                                        mxatt = invasion.stack.getDamageTowards(ArmorType.TERRITORY);
                                        tnation = invasion.stack.nation;
                                    }
                                    if(army.stack.nation.isBarbarian()) {
                                        List<SimpleChunkLocation> locs = new ArrayList<>();
                                        Town adjTown = null;
                                        outerFor:
                                        for (SimpleChunkLocation adj : chunk.getAdjacentChunks()) {
                                            if (adj.isTownChunk()&&!adj.getTown().getNation().isBarbarian()) {
                                                adjTown = adj.getTown();
                                            }
                                            if (adj.isClaimed() && !adj.isTownChunk() && !adj.getNation().isBarbarian()) {
                                                for(StructuredArmy other: StructuredArmy.armyLocationMap.getOrDefault(adj, new HashSet<>())){
                                                    if(other instanceof InvasionFlag && other.stack.nation.isBarbarian()){
                                                        continue outerFor;
                                                    }
                                                }
                                                locs.add(adj);
                                            }
                                        }
                                        if (adjTown != null){
                                            invasion.breakStructure();
                                            invasion.unregister();
                                            BarbarianInvasionManager.startBarbarianSiege(chunk, invasion.stack, adjTown);
                                            continue;
                                        }else if(locs.isEmpty()) {
                                            invasion.convertToCamp();
                                            continue;
                                        }
                                        invasion.breakStructure();
                                        invasion.unregister();
                                        ArmyStack stack = invasion.stack, newStack = new ArmyStack(stack.nation);

                                        SimpleChunkLocation loc1 = locs.get(new Random().nextInt(locs.size())), loc2 = locs.get(new Random().nextInt(locs.size()));

                                        stack.supply = 100;

                                        if(loc1.equals(loc2)){
                                            BarbarianInvasionManager.startBarbarianInvasionAt(loc1, stack);
                                        }else {
                                            newStack.supply = 100;

                                            for (ArmyStack.Unit i : new HashSet<>(stack.armies.values())) {
                                                // split the army into two parts
                                                ArmyStack.Unit transfer = new ArmyStack.Unit(i.type, i.count / 2, i.hp * ((i.count / 2) * 1.0d / i.count));
                                                newStack.addArmy(transfer);
                                                stack.removeArmy(transfer);
                                            }
                                            BarbarianInvasionManager.startBarbarianInvasionAt(loc1, stack);
                                            BarbarianInvasionManager.startBarbarianInvasionAt(loc2, newStack);
                                        }
                                    }else {
                                        invasion.convertToCamp();
                                    }
                                }
                            }
                            chunkHealths.remove(chunk);
                            Nation origNation = chunk.getNation();
                            if (tnation != null&&origNation!=null) {
                                origNation.forceUnclaim(chunk);
                                tnation.claim(chunk);
                                Nation n=tnation;
                                new BukkitRunnable() {
                                    public void run() {
                                        checkChunkAfterInvasion(chunk, n, origNation);
                                    }
                                }.runTaskAsynchronously(BetterNations.instance);
                            }
                        }
                    }.runTaskLater(BetterNations.instance, 0);
                }
            }catch (Exception e){
                Bukkit.getLogger().severe("Failed to update combat for "+chunk.toString()+"! PLEASE REPORT THE FOLLOWING TO THE DEVELOPER:");
                e.printStackTrace();
            }
        }
        if(Config.getBoolean("allow-war")&&Config.getBoolean("barbarian.enable-barbarians", false)&&Math.random()<Config.getDouble("barbarian.barbarian-invasion-chance")) BarbarianInvasionManager.attemptStartBarbarianInvasion();
        nextUpdate = System.currentTimeMillis()+1000L*Config.getInt("combat.army-tick-interval");
    }
}
