package cn.jason31416.betternations.army;

import cn.jason31416.betternations.army.structure.CampStructure;
import cn.jason31416.betternations.army.structure.UnitStructure;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.mob.SimpleMob;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ArmyStack implements Damageable, DamageSource {
    public static Map<SimplePlayer, Set<ArmyStack>> transportMissions;
    // Land army stack
    public static class Unit {
        ArmyType type;
        int count;
        double hp;
        public Unit(ArmyType type, int count){
            this.type = type;
            this.count = count;
            this.hp = type.health*count;
        }
        public Unit(ArmyType type, int count, double hp){
            this.type = type;
            this.count = count;
            this.hp = hp;
        }
        public Unit copy(){
            return new Unit(type, count, hp);
        }
    }
    public Map<ArmyType, Unit> armies = new HashMap<>();
    public Nation nation;
    public ArmyStack(Nation nation){
        this.nation = nation;
    }
    public void addArmy(ArmyType type, Integer count){
        if(armies.containsKey(type)){
            armies.get(type).count+=count;
            armies.get(type).hp+=type.health*count;
        }else{
            armies.put(type, new Unit(type, count));
        }
    }
    public void addArmy(Unit unit){
        if(armies.containsKey(unit.type)){
            armies.get(unit.type).count += unit.count;
            armies.get(unit.type).hp += unit.hp;
        }else{
            armies.put(unit.type, unit.copy());
        }
    }
    public boolean removeArmy(ArmyType armyType, Integer count){
        if(armies.containsKey(armyType)){
            if(armies.get(armyType).count>count) armies.get(armyType).count-=count;
            else if(armies.get(armyType).count==count) armies.remove(armyType);
            else return false;
            return true;
        }else return false;
    }
    public boolean removeArmyStack(ArmyStack group){
        boolean bb = true;
        for(ArmyType i: group.armies.keySet()){
            bb &= removeArmy(i, group.armies.get(i).count);
        }
        return bb;
    }
    public void addArmyStack(ArmyStack group){
        for(ArmyType i: group.armies.keySet()){
            addArmy(group.armies.get(i));
        }
    }
    public int size(){
        int count=0;
        for(ArmyType i: armies.keySet()){
            count += armies.get(i).count;
        }
        return count;
    }

    @Override
    public double getHealth() {
        double hp=0;
        for(ArmyType i: armies.keySet()){
            hp += armies.get(i).hp;
        }
        return hp;
    }

    @Override
    public void damage(double dmg) {
        double div = size();
        for(ArmyType i: new ArrayList<>(armies.keySet())) {
            Unit unit = armies.get(i);
            unit.hp -= dmg*unit.count/div;
            if(unit.hp <= 0){
                armies.remove(i);
            }
        }
    }

    @Override
    public void damage(DamageSource source) {
        double div = size();
        for(ArmyType i: new ArrayList<>(armies.keySet())){
            Unit unit = armies.get(i);
            unit.hp -= source.getDamageTowards(i.armorType)*unit.count/div;
            if(unit.hp <= 0){
                armies.remove(i);
            }
        }
    }

    @Override
    public boolean isAlive() {
        return armies.isEmpty();
    }
    @Override
    public double getDamageTowards(ArmorType type){
        PriorityQueue<Double> minHeap = new PriorityQueue<>(10);
        for (ArmyType i: armies.keySet()) {
            Double dmg = i.attack.get(type);
            if(dmg == null) continue;
            for(int j=0;j<armies.get(i).count;j++) {
                if (minHeap.size() < Config.getInt("combat.damage-units", 10) || minHeap.isEmpty()) {
                    minHeap.offer(dmg);
                } else if (dmg > minHeap.peek()) {
                    minHeap.poll();
                    minHeap.offer(dmg);
                } else {
                    break;
                }
            }
        }
        double sum = 0;
        while (!minHeap.isEmpty()) sum += minHeap.poll();
        return sum;
    }
    public boolean isEntityForm=false; // false: structure
    public SimpleMob mob=null;
    public UnitStructure structure=null;
    public SimplePlayer transportHolder=null;
    public SimpleLocation getLocation(){
        if(isEntityForm) return mob.getLocation();
        else return structure.location;
    }

    public void convertToEntity(SimplePlayer owner){
        if(isEntityForm) return;
        isEntityForm = true;
        SimpleLocation loc = getLocation();
        if(structure!=null) {
            structure.breakStructure();
            structure.unregister();
        }
        mob = SimpleMob.spawn(Config.getString("combat.transport-mob"), loc, Message.getMessage("combat.transport-name").add("nation", nation.getName()).add("units", size()).toString());
        if(transportMissions.containsKey(owner)){
            transportMissions.get(owner).add(this);
        }else{
            Set<ArmyStack> stack = new HashSet<>();
            stack.add(this);
            transportMissions.put(owner, stack);
        }
        transportHolder = owner;
    }
    public void convertToBlock(){
        if(!isEntityForm) return;
        isEntityForm = false;
        SimpleLocation loc = getLocation();
        if(transportMissions.containsKey(transportHolder)){
            transportMissions.get(transportHolder).remove(this);
            if(transportMissions.get(transportHolder).isEmpty()) transportMissions.remove(transportHolder);
        }
        mob.kill();
        structure = new CampStructure(loc, this);
        structure.place();
    }
}
