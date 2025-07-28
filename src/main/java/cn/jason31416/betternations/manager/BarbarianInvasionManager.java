package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.betternations.army.ArmyType;
import cn.jason31416.betternations.army.states.InvasionFlag;
import cn.jason31416.betternations.army.states.SiegeFlag;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BarbarianInvasionManager {
    private static Nation barbarianNation=null;
    public static Nation getBarbarianNation(){
        if(barbarianNation==null) {
            barbarianNation = Nation.getNation(Config.getString("barbarian.barbarian-nation-name", "Barbarians"));
            if (barbarianNation == null) {
                barbarianNation = Nation.createNation(SimplePlayer.of("_#Neutral.Player"), Config.getString("barbarian.barbarian-nation-name", "Barbarians"));
            }
        }
        return barbarianNation;
    }
    public static void startBarbarianInvasionAt(SimpleChunkLocation chunkLocation, ArmyStack stack){
        if(chunkLocation.isTownChunk()||!chunkLocation.isClaimed()) return;
        if(getBarbarianNation().nationalChunks.contains(chunkLocation)) return;
        Nation nation = chunkLocation.getNation();
        if(!getBarbarianNation().getRelation(nation).equals(Relation.ENEMY)){
            getBarbarianNation().setRelation(nation, Relation.ENEMY);
        }
        Random rand = new Random();
        SimpleLocation loc = SimpleLocation.of(chunkLocation.getBukkitWorld().getHighestBlockAt(chunkLocation.x()*16+rand.nextInt(16), chunkLocation.z()*16+rand.nextInt(16)));
        while(loc.y()<loc.world().getBukkitWorld().getMaxHeight()&&loc.getBlockMaterial()!= Material.AIR){
            loc = loc.getRelative(0, 1, 0);
        }
        if(loc.y()>=loc.world().getBukkitWorld().getMaxHeight()){
            loc = loc.getRelative(0, -1, 0);
        }
        SimpleLocation location = loc;
        new BukkitRunnable(){
            @Override
            public void run() {
                InvasionFlag c = new InvasionFlag();
                c.stack = stack;
                c.location = location;
                stack.curHolder = c;
                c.place();
            }
        }.runTaskLater(BetterNations.instance, 1);

        Message.getMessage("combat.barbarian-invasion-started").add("nation", chunkLocation.getNation().getName()).add("location", loc.x()+","+loc.y()+","+loc.z())
                .send(chunkLocation.getNation().getMembers());
    }
    public static void startBarbarianSiege(SimpleChunkLocation chunkLocation, ArmyStack stack, Town town){
        Nation nation = town.getNation();
        if(!getBarbarianNation().getRelation(nation).equals(Relation.ENEMY)){
            getBarbarianNation().setRelation(nation, Relation.ENEMY);
        }
        SimpleLocation loc = SimpleLocation.of(chunkLocation.getBukkitWorld().getHighestBlockAt(chunkLocation.x()*16+8, chunkLocation.z()*16+8));
        while(loc.y()<loc.world().getBukkitWorld().getMaxHeight()&&loc.getBlockMaterial()!= Material.AIR){
            loc = loc.getRelative(0, 1, 0);
        }
        if(loc.y()>=loc.world().getBukkitWorld().getMaxHeight()){
            loc = loc.getRelative(0, -1, 0);
        }
        SimpleLocation location = loc;
        new BukkitRunnable(){
            @Override
            public void run() {
                SiegeFlag c = new SiegeFlag();
                c.target = town;
                c.location = location;
                c.stack = stack;
                stack.curHolder = c;
                c.place();
            }
        }.runTaskLater(BetterNations.instance, 1);
    }
    public static void attemptStartBarbarianInvasion() {
        if(Nation.chunkNationMap.isEmpty()) return;
        List<SimpleChunkLocation> chunkLocations = new ArrayList<>(Nation.chunkNationMap.keySet());
        SimpleChunkLocation rand = chunkLocations.get(new Random().nextInt(chunkLocations.size()));
        if(rand.isTownChunk()) return;
        if(getBarbarianNation().nationalChunks.contains(rand)) return;
        outer:
        {
            for (SimpleChunkLocation adj : rand.getAdjacentChunks()) {
                if (!adj.isClaimed()|| Objects.equals(adj.getNation(), getBarbarianNation())) break outer;
            }
            return;
        }

        ArmyStack stack = new ArmyStack(getBarbarianNation());
        stack.supply = 100;
        if(!Config.config.contains("barbarian.army.units")) return;

        List<String> possibleTypes = new ArrayList<>(Config.config.getConfigurationSection("barbarian.army.units").getKeys(false));
        int tot = 0;
        for(String type:possibleTypes){
            tot += Config.getInt("barbarian.army.units."+type, 0);
        }
        for(int i=0;i<Config.getInt("barbarian.army.initial-count", 10);i++){
            int num = new Random().nextInt(tot), cur=0;
            while(cur<possibleTypes.size()&&num>=Config.getInt("barbarian.army.units."+possibleTypes.get(cur), 0)){
                num -= Config.getInt("barbarian.army.units."+possibleTypes.get(cur), 0);
                cur++;
            }
            String type = possibleTypes.get(cur);
            stack.addArmy(ArmyType.armyTypes.get(type), 1);
        }
        startBarbarianInvasionAt(rand, stack);
    }
}
