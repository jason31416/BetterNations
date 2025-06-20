package cn.jason31416.betternations.structure.types;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.betternations.army.ArmyType;
import cn.jason31416.betternations.army.states.ArmyCamp;
import cn.jason31416.betternations.army.states.TransportArmy;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.structure.PlaceableStructure;
import cn.jason31416.planetlib.Utils;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.item.CustomItemType;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UnitProductionStructure extends PlaceableStructure {
    public static Map<String, Map<String, Recipe>> recipes = new HashMap<>();
    public static Map<String, Material> materialMap = new HashMap<>();
    public record Recipe(ArmyType armyType, long duration) {}
    public Map<ArmyType, ArmyStack.Unit> armyStorage = new HashMap<>();
    public String currentProducing=null;
    public long finishTime=0;
    public String type;

    public String getHologramText(){
        return Message.getMessage("structure."+type+".hologram").toString();
    }

    @Override
    public Material getMaterial() {
        return materialMap.get(type);
    }
    @Override
    public void breakStructure() {
        if(Bukkit.isPrimaryThread()) {
            hologram.removeHologram();
            location.setBlockMaterial(Material.AIR);
        }else{
            new BukkitRunnable() {
                public void run(){
                    hologram.removeHologram();
                    location.setBlockMaterial(Material.AIR);
                }
            }.runTaskLater(BetterNations.instance, 0);
        }
        exists = false;
        if(location.getBukkitLocation().getWorld()==null||!CustomItemType.itemTypes.containsKey(type)) return;
        location.getBukkitLocation().getWorld().dropItem(location.getBukkitLocation(), ItemType.getItemType(type).getItemStack());
    }
    @Override
    public boolean serialize(IDataItem dataItem) {
        if(currentProducing!=null) dataItem.set("cp", currentProducing);
        dataItem.set("ct", finishTime);
        dataItem.set("tp", type);
        ArrayList<String> armiesList = new ArrayList<>();
        for(ArmyType i: armyStorage.keySet()){
            armiesList.add(i.id + ":" + armyStorage.get(i).count);
        }
        dataItem.set("armies", String.join(";", armiesList));
        return true;
    }
    @Override
    public void deserialize(IDataItem dataItem) {
        currentProducing = dataItem.getString("cp");
        finishTime = dataItem.getLong("ct");
        type = dataItem.getString("tp");

        armyStorage.clear();
        for(String armyStr : dataItem.getString("armies").split(";")) {
            if (armyStr.isEmpty()) continue;
            String[] armyArr = armyStr.split(":");
            ArmyType tp = ArmyType.armyTypes.get(armyArr[0]);
            if(tp == null){
                Bukkit.getLogger().severe("Unknown army type!");
                continue;
            }
            armyStorage.put(tp, new ArmyStack.Unit(tp, Integer.parseInt(armyArr[1])));
        }
    }
    private void checkComplete(){
        Map<String, Recipe> recipeMap = recipes.get(this.type);
        if(System.currentTimeMillis()>=finishTime&&currentProducing!=null){
            if(recipeMap.containsKey(currentProducing)) {
                ArmyType type = recipeMap.get(currentProducing).armyType;
                if (!armyStorage.containsKey(type)) {
                    armyStorage.put(type, new ArmyStack.Unit(type, 1));
                } else {
                    armyStorage.get(type).count += 1;
                    armyStorage.get(type).hp += type.health;
                }
            }
            currentProducing = null;
            finishTime = 0;
        }
    }
    @Override
    public boolean processInteraction(InteractionType type, SimplePlayer player) {
        if(type==InteractionType.INTERACT){
            Map<String, Recipe> recipeMap = recipes.get(this.type);
            if(location.getChunkLocation().isTownChunk()&&
                    player.hasPermission(Permission.STRUCTURE, location)){
                checkComplete();
                ItemStack hand = player.getPlayer().getInventory().getItemInMainHand();
                if(recipeMap.containsKey(ItemType.getItemType(hand).getName())){
                    if(currentProducing==null){
                        currentProducing = ItemType.getItemType(hand).getName();
                        finishTime = recipeMap.get(ItemType.getItemType(hand).getName()).duration+System.currentTimeMillis();
                        player.getPlayer().getInventory().getItemInMainHand().setAmount(hand.getAmount()-1);
                    }
                    return true;
                }
                new GUISession(player) {
                    ItemStack ti=null;
                    @Override
                    public void constructGUI(String guiID, GUI gui) {
                        if(ti==null) ti = gui.getItems("training").toBukkitItem();
                        gui.placeholder("structure_name", getHologramText());
                        checkComplete();
                        int pos = 28;
                        for(ArmyType type: armyStorage.keySet()){
                            gui.addItem("army-"+pos, pos, armyStorage.get(type).getItemStack());
                            pos ++;
                            if(pos%9==8) pos += 2;
                        }
                        gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                        gui.getItems("extract")
                                .setMaterial(armyStorage.isEmpty()?Material.GRAY_WOOL:Material.LIME_WOOL)
                                .setClickHandler((session, action, event) -> {
                                    if(!armyStorage.isEmpty()&&player.getNation()!=null&&player.getNation()==location.getChunkLocation().getNation()){
                                        ArmyStack stack = new ArmyStack(player.getNation());
                                        stack.armies = new HashMap<>(armyStorage);
                                        stack.supply = stack.getMaxSupply();
                                        armyStorage.clear();
                                        TransportArmy.spawn(location.getRelative(0.5, 1, 0.5), player, stack);
                                    }
                                });
                        gui.getItems("material")
                                .setMaterial(currentProducing==null?Material.LIME_STAINED_GLASS_PANE:Material.ORANGE_STAINED_GLASS_PANE)
                                .setClickHandler((session, action, event) -> {
                            if(event.getCursor()!=null&&currentProducing==null){
                                for(String type: recipeMap.keySet()){
                                    if(ItemType.getItemType(event.getCursor()).getName().equals(type)){
                                        event.getCursor().setAmount(event.getCursor().getAmount()-1);
                                        currentProducing = type;
                                        finishTime = recipeMap.get(type).duration+System.currentTimeMillis();
                                        break;
                                    }
                                }
                            }
                        });
                        if(currentProducing!=null) gui.getItems("training")
                                    .setMaterial(Material.IRON_SWORD)
                                .setName(Message.getMessage("combat.training.progress-item.name").add("type", recipeMap.get(currentProducing).armyType.name).toString())
                                .setLore(MessageLoader.getList("combat.training.progress-item.lore").add("time", Utils.formatSeconds((int)((finishTime - System.currentTimeMillis()) / 1000))).asList());
                        else gui.getItems("training").setItemStack(ti);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(!GUISession.sessions.containsKey(player)||
                                    GUISession.sessions.get(player).gui!=gui) return;
                                for(int pos = 28;pos<44;){
                                    gui.container.remove(pos);
                                    pos ++;
                                    if(pos%9==8) pos += 2;
                                }
                                constructGUI(guiID, gui);
                                gui.update();
                            }
                        }.runTaskLater(BetterNations.instance, 5L);
                    }
                }.display("army-production");
            }else{
                Message.getMessage("town.cannot-build").sendActionbar(player);
            }
        }else if(type == InteractionType.BREAK){
            breakStructure();
            unregister();
            if(!armyStorage.isEmpty()) {
                ArmyStack stack = new ArmyStack(player.getNation());
                stack.armies = new HashMap<>(armyStorage);
                stack.supply=stack.getMaxSupply();
                armyStorage.clear();
                ArmyCamp c = new ArmyCamp();
                c.stack = stack;
                c.location = location;
                stack.curHolder = c;
                c.place();
            }
            return false;
        }
        return true;
    }
}
