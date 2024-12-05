package cn.jason31416.betternations.army;

import cn.jason31416.betternations.army.states.ArmyCamp;
import cn.jason31416.betternations.army.states.ArmyStackHolder;
import cn.jason31416.betternations.army.states.StructuredArmy;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ArmyStack implements Damageable, DamageSource {
    // Land army stack
    public static class Unit {
        public ArmyType type;
        public int count;
        public double hp;
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
        public ItemStack getItemStack(){
            GUI.Item item = new GUI.Item("unit");
            item.setName(new StringMessage("&f"+type.name+" &7x"+count).toString());
            item.setQuantity(count);
            item.setLore(MessageLoader.getList("combat.unit.item-lore")
                            .add("health", Math.round(hp*10)/10.0)
                            .add("max_health", count*type.health)
                            .asList());
            item.setMaterial(type.icon);
            return item.toBukkitItem();
        }
        public Unit copy(){
            return new Unit(type, count, hp);
        }
    }
    public Map<ArmyType, Unit> armies = new HashMap<>();
    public Nation nation;
    public ArmyStackHolder curHolder = null;
    public double supply;
    public Queue<Object> damageQueue = new ArrayDeque<>();
    public ArmyStack(Nation nation){
        this.nation = nation;
    }
    public void addArmy(ArmyType type, Integer count, Double health){
        if(armies.containsKey(type)){
            armies.get(type).count+=count;
            armies.get(type).hp+=health;
        }else{
            armies.put(type, new Unit(type, count, health));
        }
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
    public double getMaxSupply() {
        double total=0;
        for(ArmyType i: armies.keySet()){
            total += i.maxSupply*armies.get(i).count;
        }
        return total;
    }
    public double getSupplyConsumption() {
        double total=0;
        for(ArmyType i: armies.keySet()){
            total += i.consumption*armies.get(i).count;
        }
        return total;
    }
    public double getMaxHealth() {
        double hp=0;
        for(ArmyType i: armies.keySet()){
            hp += armies.get(i).count*i.health;
        }
        return hp;
    }
    @Override
    public double getHealth() {
        double hp=0;
        for(ArmyType i: armies.keySet()){
            hp += armies.get(i).hp;
        }
        return hp;
    }
    public void displayGUI(GUI gui, int starting, int ending){
        int pos = starting;
        for(ArmyType type: armies.keySet()){
            if(pos>=ending) return;
            gui.addItem("army-"+pos, pos, armies.get(type).getItemStack());
            pos ++;
            if(pos%9==8) pos += 2;
        }
    }

    private boolean isBreaking(){
        return (curHolder instanceof StructuredArmy sa) && sa.runnable != null;
    }

    public void processDamageQueue(){
        if(isBreaking()) return;
        while(!damageQueue.isEmpty()){
            Object item = damageQueue.poll();
            if(item instanceof Double dmg){
                damage(dmg);
            }else if(item instanceof DamageSource dmg){
                damage(dmg);
            }
        }
    }
    public String getStackName(){
        return Message.getMessage("combat.stack-name").add("nation", nation.getName()).add("count", size()).toString();
    }
    public ItemStack getItemDisplay(){
        if(armies.isEmpty()){
            GUI.Item ret = new GUI.Item("");
            ret.setMaterial(Material.BARRIER)
                    .setName(" ");
            return ret.toBukkitItem();
        }
        GUI.Item ret = new GUI.Item("");
        ret.setMaterial(armies.keySet().stream().toList().get(0).icon)
                .setName(getStackName())
                .setLore(MessageLoader.getList("combat.stack-lore")
                        .add("health", Math.round(getHealth()*10)/10.0)
                        .add("max_health", getMaxHealth())
                        .add("damage_unarmed", getDamageTowards(ArmorType.UNARMED))
                        .add("damage_armored", getDamageTowards(ArmorType.ARMORED))
                        .add("damage_territory", getDamageTowards(ArmorType.TERRITORY))
                        .asList());
        return ret.toBukkitItem();
    }

    @Override
    public void damage(double dmg) {
        if(isBreaking()){
//            damageQueue.add(dmg);
            return;
        }
        double div = size();
        for(ArmyType i: new ArrayList<>(armies.keySet())) {
            Unit unit = armies.get(i);
            unit.hp -= dmg*unit.count/div;
            while(unit.hp <= (unit.count-1)*i.health){
                unit.count -= 1;
            }
            if(unit.count <= 0){
                armies.remove(i);
            }
        }
    }

    @Override
    public void damage(DamageSource source) {
        if(isBreaking()){
//            damageQueue.add(source);
            return;
        }
        double div = size();
        for(ArmyType i: new ArrayList<>(armies.keySet())){
            Unit unit = armies.get(i);
            unit.hp -= source.getDamageTowards(i.armorType)*unit.count/div;
            while(unit.hp <= (unit.count-1)*i.health){
                unit.count -= 1;
            }
            if(unit.count <= 0){
                armies.remove(i);
            }
        }
    }
    public SimpleLocation getLocation(){
        return curHolder.getLocation();
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
    public void destroy(){
        // todo: register/destruction of army stacks
    }
    public void serialize(IDataItem dataItem){
        List<String> armyList=new ArrayList<>();
        for(ArmyType i: armies.keySet()){
            armyList.add(i.id+":"+armies.get(i).count+":"+armies.get(i).hp);
        }
        dataItem.set("a_bel", nation.getId().toString());
        dataItem.set("a_cont", String.join(";", armyList));
    }
    public static ArmyStack deserialize(IDataItem dataItem){
        ArmyStack stack = new ArmyStack(Nation.getNation(UUID.fromString(dataItem.getString("a_bel"))));
        for(String i: dataItem.getString("a_cont").split(";")){
            if(i.isEmpty()) continue;
            String[] unit_string = i.split(":");
            if(ArmyType.armyTypes.containsKey(unit_string[0])) stack.addArmy(ArmyType.armyTypes.get(unit_string[0]), Integer.parseInt(unit_string[1]), Double.parseDouble(unit_string[2]));
            else Bukkit.getLogger().severe("Error loading armystack: Army type not found! Please check your configuration.");
        }
        return stack;
    }
}
