package cn.jason31416.betternations.army.states;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.ArmorType;
import cn.jason31416.betternations.army.BreakCampRunnable;
import cn.jason31416.betternations.manager.ArmyUpdateManager;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.structure.Hologram;
import cn.jason31416.planetlib.ColorUtils;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.Utils;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class SiegeFlag extends StructuredArmy {
    public Town target;
    public SiegeFlag(){}
    public SiegeFlag(Town target){
        this.target = target;
    }
    public boolean serialize(IDataItem dataItem){
        if(!target.isAlive()) return false;
        dataItem.set("targ", target.getId().toString());
        return super.serialize(dataItem);
    }
    public void deserialize(IDataItem dataItem){
        super.deserialize(dataItem);
        target = Town.getTown(UUID.fromString(dataItem.getString("targ")));
    }
    @Override
    public String getHologramText() {
        return Message.getMessage("combat.siege-name").add("nation", stack.nation.getColorTag()+stack.nation.getName()).add("units", stack.size()).add("town", target.getNation().getColorTag()+target.getName()).toString();
    }
    @Override
    public void place(){
        location.setBlockMaterial(getMaterial());
        hologram = Hologram.createHologram(SimpleLocation.of(location.getBlock().getLocation().add(0.5, 2.3, 0.5)), getHologramText());
        register();
    }
    @Override
    public Material getMaterial() {
        return ColorUtils.getClosest(stack.nation.getColor()).banner();
    }

    @Override
    public boolean processInteraction(InteractionType type, SimplePlayer player) {
        if(type == InteractionType.INTERACT){
            SiegeFlag siege = this;
            new GUISession(player){
                @Override
                public void constructGUI(String guiID, GUI gui) {
                    gui.placeholder("town", target.getName());
                    switch (guiID) {
                        case "siege-main": {
                            stack.displayGUI(gui, 28, 44);
                            gui.getItems("army-overview").setItemStack(stack.getItemDisplay(ArmyCamp.supplyGain(location.getChunkLocation(), stack.nation)));
                            gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                            if(player.getNation()==stack.nation&&player.hasPermission(Permission.MANAGE_ARMY)) gui.getItems("action-page").setClickHandler(new GUI.SwitchGuiRunnable("siege-actions"));
                            else gui.getItems("action-page").setMaterial(Material.BARRIER);
                            double mxhp = target.getMaxHealth(), chunkhp=target.getHealth();
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
                                    Nation nation = target.getNation();
                                    if(nation == null) continue;
                                    gui.addItem("prog-"+i, Message.getMessage("combat.progress.invasion.town.name").add("defender", nation.getColorTag()+nation.getName()).add("town", target.getName()).toString(), pos, Material.SHIELD, 1)
                                            .setLore(MessageLoader.getList("combat.progress.invasion.town.lore")
                                                    .add("health", chunkhp)
                                                    .add("max_health", mxhp)
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
                                    if(!siege.running){
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
                        case "siege-actions": {
                            gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                            if(isInCombat()) gui.getItems("extract").setMaterial(Material.BARRIER);
                            gui.getItems("extract").setClickHandler((session, action, evt) -> {
                                if(isInCombat()){
                                    Message.getMessage("combat.cannot-move-unit-during-combat").send(player);
                                    session.close();
                                    return;
                                }
                                if(stack.curHolder == siege){
                                    TransportArmy.spawn(location, player, stack);
                                    siege.breakStructure();
                                    siege.unregister();
                                    session.close();
                                }
                            });
                        }
                    }
                }
            }.display("siege-main");
        }else if(type == InteractionType.BREAK) {
            if(stack.nation.getRelation(player.getNation()) == Relation.ENEMY&&runnable == null){
                player.sendMessage(Message.getMessage("combat.cannot-break-camps-in-nation"));
//                runnable = new BreakCampRunnable(player, this);
//                runnable.runTaskTimer(BetterNations.instance, 2, 2);
            }
            return false;
        }
        return true;
    }

    public void convertToCamp(){
        ArmyCamp camp = new ArmyCamp();
        camp.stack = stack;
        camp.location = location;
        breakStructure();
        unregister();
        camp.place();
    }
}
