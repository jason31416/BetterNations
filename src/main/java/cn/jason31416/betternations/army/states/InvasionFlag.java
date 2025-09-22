package cn.jason31416.betternations.army.states;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.ArmorType;
import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.betternations.army.BreakCampRunnable;
import cn.jason31416.betternations.manager.ArmyUpdateManager;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.structure.Hologram;
import cn.jason31416.planetlib.ColorUtils;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.Utils;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Locale;

public class InvasionFlag extends StructuredArmy {
    public static enum AutomationMode {
        SPEARHEAD,
        PUSH,
        NONE
    }
    public AutomationMode automation=AutomationMode.NONE;

    public InvasionFlag(){}
    @Override
    public String getHologramText() {
        return Message.getMessage("combat.invasion-name").add("nation", stack.nation.getColorTag()+stack.nation.getName()).add("units", stack.size()).toString();
    }
    @Override
    public void place(){
        location.setBlockMaterial(getMaterial());
        hologram = Hologram.createHologram(SimpleLocation.of(location.getBlock().getLocation().add(0.5, 2.3, 0.5)), getHologramText());
        register();
    }
    @Override
    public boolean serialize(IDataItem dataItem) {
        dataItem.set("chunkhp", ArmyUpdateManager.chunkHealths.getOrDefault(location.getChunkLocation(), Config.getDouble("combat.chunk-hp", 10.0)));
        dataItem.set("automation", automation.name());
        return super.serialize(dataItem);
    }

    @Override
    public void deserialize(IDataItem dataItem) {
        super.deserialize(dataItem);
        ArmyUpdateManager.chunkHealths.put(location.getChunkLocation(), dataItem.getDouble("chunkhp"));
        automation = AutomationMode.valueOf(dataItem.getString("automation"));
    }
    @Override
    public Material getMaterial() {
        return ColorUtils.getClosest(stack.nation.getColor()).banner();
    }

    @Override
    public boolean processInteraction(InteractionType type, SimplePlayer player) {
        if(type == InteractionType.INTERACT){
            InvasionFlag invasion = this;
            new GUISession(player){
                @Override
                public void constructGUI(String guiID, GUI gui) {
                    switch (guiID) {
                        case "invasion-main": {
                            stack.displayGUI(gui, 28, 44);
                            gui.getItems("army-overview").setItemStack(stack.getItemDisplay(ArmyCamp.supplyGain(location.getChunkLocation(), stack.nation)));
                            gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                            gui.getItems("invasion-automation")
                                    .setMaterial(automation == AutomationMode.SPEARHEAD? Material.LIME_WOOL : automation == AutomationMode.PUSH? Material.MAGENTA_WOOL : Material.GRAY_WOOL)
                                    .setLore(List.of(MessageLoader.getMessage("combat.invasion-automation."+automation.name().toLowerCase(Locale.ROOT)).toString()))
                                    .setClickHandler((session, action, evt) -> {
                                        if(!player.hasPermission(Permission.MANAGE_ARMY)||player.getNation()!=stack.nation) return;
                                        if(automation == AutomationMode.SPEARHEAD) automation = AutomationMode.PUSH;
                                        else if(automation == AutomationMode.PUSH) automation = AutomationMode.NONE;
                                        else automation = AutomationMode.SPEARHEAD;
                                        constructGUI(guiID, gui);
                                        gui.update();
                                    });
                            if(player.getNation()==stack.nation&&player.hasPermission(Permission.MANAGE_ARMY)) gui.getItems("action-page").setClickHandler(new GUI.SwitchGuiRunnable("invasion-actions"));
                            else gui.getItems("action-page").setMaterial(Material.BARRIER);
                            double mxhp = Config.getDouble("combat.chunk-hp", 20), chunkhp=Math.round(ArmyUpdateManager.chunkHealths.getOrDefault(invasion.location.getChunkLocation(), mxhp)*100)/100.0;
                            if(chunkhp>mxhp) return;
                            int cnt = (int) ((mxhp-chunkhp)*5/mxhp);
                            for(int i=0;i<7;i++){
                                int pos = i+10;
                                if(i<cnt){
                                    gui.addItem("prog-"+i, " ", pos, Material.RED_STAINED_GLASS_PANE, 1);
                                }else if(i==cnt){
                                    gui.addItem("prog-"+i, Message.getMessage("combat.progress.invasion.attack.name").add("attacker", stack.nation.getColorTag()+stack.nation.getName()).toString(), pos, Material.IRON_SWORD, 1)
                                            .setLore(MessageLoader.getList("combat.progress.invasion.attack.lore")
                                                    .add("next_attack", Utils.formatSeconds((int)((ArmyUpdateManager.nextUpdate-System.currentTimeMillis())/1000L)))
                                                    .add("health", Math.round(stack.getHealth()*10)/10.0)
                                                    .add("damage", stack.getDamageTowards(ArmorType.TERRITORY))
                                                    .asList());
                                }else if(i==cnt+1){
                                    Nation nation = location.getChunkLocation().getNation();
                                    if(nation == null) continue;
                                    gui.addItem("prog-"+i, Message.getMessage("combat.progress.invasion.defend.name")
                                                    .add("chunk", location.world().getName()+","+location.getChunkLocation().x()+","+location.getChunkLocation().z())
                                                    .add("defender", nation.getColorTag()+nation.getName()).toString(), pos, ColorUtils.getClosest(nation.getColor()).banner(), 1)
                                            .setLore(MessageLoader.getList("combat.progress.invasion.defend.lore")
                                                    .add("health", chunkhp)
                                                    .asList());
                                }else{
                                    gui.addItem("prog-"+i, " ", pos, Material.BLUE_STAINED_GLASS_PANE, 1);
                                }
                            }
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if(!GUISession.sessions.containsKey(player)||
                                            GUISession.sessions.get(player).gui!=gui) return;
                                    if(!invasion.running){
                                        player.getPlayer().closeInventory();
                                        return;
                                    }
                                    for(int pos = 28;pos<44;){
                                        gui.container.remove(pos);
                                        pos ++;
                                        if(pos%9==8) pos += 2;
                                    }
                                    constructGUI(guiID, gui);
                                    gui.update();
                                }
                            }.runTaskLater(BetterNations.instance, 4L);
                        }
                        case "invasion-actions": {
                            gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                            if(isInCombat()) gui.getItems("extract").setMaterial(Material.BARRIER);
                            gui.getItems("extract").setClickHandler((session, action, evt) -> {
                                if(isInCombat()){
                                    Message.getMessage("combat.cannot-move-unit-during-combat").send(player);
                                    session.close();
                                    return;
                                }
                                if(stack.curHolder == invasion){
                                    TransportArmy.spawn(location, player, stack);
                                    invasion.breakStructure();
                                    invasion.unregister();
                                    session.close();
                                }
                            });
                        }
                    }
                }
            }.display("invasion-main");
        }else if(type == InteractionType.BREAK) {
            if(stack.nation.getRelation(player.getNation()) == Relation.ENEMY&&runnable == null){
                runnable = new BreakCampRunnable(player, this);
                runnable.runTaskTimer(BetterNations.instance, 2, 2);
            }
            return false;
        }
        return true;
    }

    public void convertToCamp(){
        if(Bukkit.isPrimaryThread()) {
            ArmyCamp camp = new ArmyCamp();
            camp.stack = stack;
            camp.location = location;
            breakStructure();
            unregister();
            camp.place();
        }else{
            Bukkit.getScheduler().runTask(BetterNations.instance, this::convertToCamp);
        }
    }
}
