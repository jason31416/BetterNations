package cn.jason31416.betternations.army.states;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.betternations.army.ArmyType;
import cn.jason31416.betternations.army.BreakCampRunnable;
import cn.jason31416.betternations.manager.FromToAnimationManager;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.structure.types.Granary;
import cn.jason31416.planetlib.ColorUtils;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;

import java.util.List;

public class ArmyCamp extends StructuredArmy {
    public ArmyCamp(){}
    @Override
    public String getHologramText() {
        return Message.getMessage("combat.camp-name").add("nation", stack.nation.getColorTag()+stack.nation.getName()).add("units", stack.size()).toString();
    }
    @Override
    public Material getMaterial() {
        return ColorUtils.getClosest(stack.nation.getColor()).stained_glass();
    }
    public static double supplyGain(SimpleChunkLocation loc, Nation nation){
        if(nation.getRelation(loc.getNation()) == Relation.ALLY){
            if(Granary.granaries.containsKey(loc)){
                double ret=0;
                for(Granary i: Granary.granaries.get(loc)){
                    ret += Math.min(Config.getDouble("combat.supply-regain-speed"), i.supply);
                }
                return ret;
            }
        }
        return 0;
    }

    @Override
    public boolean processInteraction(InteractionType type, SimplePlayer player) {
        if(runnable != null) return false;
        if(type == InteractionType.INTERACT){
            ArmyCamp camp = this;
            new GUISession(player){
                final ArmyStack selected=new ArmyStack(stack.nation);
                @Override
                public void constructGUI(String guiID, GUI gui) {
                    switch (guiID) {
                        case "camped-army": {
                            stack.displayGUI(gui, 10, 44);
                            gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                            gui.getItems("army-overview").setItemStack(stack.getItemDisplay(ArmyCamp.supplyGain(location.getChunkLocation(), stack.nation)));
                            if(isInCombat()){
                                gui.getItems("combat-overview").setClickHandler(new GUI.SwitchGuiRunnable("combat-overview"));
                            }else gui.getItems("combat-overview").setMaterial(Material.BARRIER);
                            if(player.getNation()==stack.nation&&player.hasPermission(Permission.MANAGE_ARMY)) gui.getItems("action-page").setClickHandler(new GUI.SwitchGuiRunnable("camped-actions"));
                            else gui.getItems("action-page").setMaterial(Material.BLACK_WOOL).setName("&c没有权限").setLore(List.of());
                            break;
                        }
                        case "combat-overview": {
                            int lcnt=10, rcnt=14;
                            for(StructuredArmy i: StructuredArmy.armyLocationMap.get(location.getChunkLocation())){
                                if(i.stack.nation.getRelation(stack.nation)==Relation.ENEMY){
                                    gui.addItem("stack-"+rcnt, rcnt, i.getStack().getItemDisplay(supplyGain(i.location.getChunkLocation(), i.getStack().nation)));
                                    rcnt++;
                                    if(rcnt%9==8) rcnt+=6;
                                }else if(i==camp||i.isInCombat()){
                                    gui.addItem("stack-"+lcnt, lcnt, i.getStack().getItemDisplay(supplyGain(i.location.getChunkLocation(), i.getStack().nation)));
                                    lcnt++;
                                    if(lcnt%9==4) lcnt+=6;
                                }
                            }
                            gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                        }
                        case "camped-actions": {
                            gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                            if(isInCombat()){
                                gui.getItems("extract").setMaterial(Material.BARRIER);
                                gui.getItems("split").setMaterial(Material.BARRIER);
                            }
                            gui.getItems("extract").setClickHandler((session, action, evt) -> {
                                    if(isInCombat()){
                                        Message.getMessage("combat.cannot-move-unit-during-combat").send(player);
                                        session.close();
                                        return;
                                    }
                                    if(stack.curHolder == camp){
                                    TransportArmy.spawn(location, player, stack);
                                    camp.breakStructure();
                                    camp.unregister();
                                    session.close();
                                }
                            });
                            gui.getItems("split").setClickHandler((session, action, evt) -> {
                                if(isInCombat()){
                                    Message.getMessage("combat.cannot-move-unit-during-combat").send(player);
                                    session.close();
                                    return;
                                }
                                session.display("camped-split");
                            });
                            break;
                        }
                        case "camped-split": {
                            int pos = 10;
                            for(ArmyType type: stack.armies.keySet()){
                                if(pos>=44) return;
                                int p=pos;
                                gui.addItem("army-"+pos, pos, stack.armies.get(type).getItemStack())
                                        .setQuantity(selected.armies.getOrDefault(type, new ArmyStack.Unit(type, 1)).count)
                                        .setLore(MessageLoader.getList("combat.unit.selecting-item-lore")
                                                .add("health", Math.round(stack.armies.get(type).hp*10)/10.0)
                                                .add("max_health", stack.armies.get(type).count*type.health)
                                                .add("count", selected.armies.getOrDefault(type, new ArmyStack.Unit(type, 0)).count)
                                                .add("total", stack.armies.get(type).count)
                                                .asList())
                                        .setClickHandler((session, action, event) -> {
                                            if(action == InventoryAction.PICKUP_ALL&&(!selected.armies.containsKey(type)||selected.armies.get(type).count<stack.armies.get(type).count)){
                                                selected.addArmy(type, 1);
                                            }else if(action == InventoryAction.PICKUP_HALF){
                                                selected.removeArmy(type, 1);
                                            }else{
                                                return;
                                            }
                                            gui.getItems("army-overview").setItemStack(selected.getItemDisplay());
                                            gui.getItem(p)
                                                    .setLore(MessageLoader.getList("combat.unit.selecting-item-lore")
                                                        .add("health", Math.round(stack.armies.get(type).hp*10)/10.0)
                                                        .add("max_health", stack.armies.get(type).count*type.health)
                                                        .add("count", selected.armies.getOrDefault(type, new ArmyStack.Unit(type, 0)).count)
                                                        .add("total", stack.armies.get(type).count)
                                                        .asList())
                                                    .setQuantity(selected.armies.getOrDefault(type, new ArmyStack.Unit(type, 1)).count);
                                            gui.update();
                                        });
                                pos ++;
                                if(pos%9==8) pos += 2;
                            }
                            gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                            gui.getItems("army-overview").setItemStack(selected.getItemDisplay());
                            gui.getItems("confirm").setClickHandler((session, action, event) -> {
                                if(isInCombat()){
                                    Message.getMessage("combat.cannot-move-unit-during-combat").send(player);
                                    session.close();
                                    return;
                                }
                                if(selected.size()>0){
                                    double sup = 0;
                                    for(ArmyType tp: selected.armies.keySet()){
                                        if(!stack.armies.containsKey(tp)){
                                            player.sendMessage(Message.getMessage("combat.invalid-selection"));
                                            return;
                                        }
                                        selected.armies.get(tp).hp = stack.armies.get(tp).hp/stack.armies.get(tp).count*selected.armies.get(tp).count;
                                        stack.armies.get(tp).hp -= selected.armies.get(tp).hp;
                                        sup += stack.supply/stack.getMaxSupply()*tp.maxSupply*selected.armies.get(tp).count;
                                        if(!stack.removeArmy(tp, selected.armies.get(tp).count)){
                                            player.sendMessage(Message.getMessage("combat.invalid-selection"));
                                            return;
                                        }
                                    }
                                    stack.supply -= sup;
                                    selected.supply = sup;
                                    TransportArmy.spawn(location.getRelative(0, 1, 0), player, selected);
                                    if(stack.size()<=0){
                                        unregister();
                                        breakStructure();
                                    }else{
                                        hologram.setText(getHologramText());
                                    }
                                }
                                player.getPlayer().closeInventory();
                            });
                            break;
                        }
                    }
                }
            }.display("camped-army");
        }else if(type == InteractionType.BREAK) {
            if(stack.curHolder == this&&player.getNation()==stack.nation){
                if(!isInCombat()) {
                    TransportArmy.spawn(location, player, stack);
                    breakStructure();
                    unregister();
                }else{
                    Message.getMessage("combat.cannot-move-unit-during-combat").send(player);
                }
            }else if(stack.nation.getRelation(player.getNation()) == Relation.ENEMY&&runnable == null){
                if(location.getChunkLocation().isTownChunk()&&location.getChunkLocation().getNation()==stack.nation){
                    player.sendMessage(Message.getMessage("combat.cannot-break-camps-in-town"));
                }else {
                    runnable = new BreakCampRunnable(player, this);
                    runnable.runTaskTimer(BetterNations.instance, 2, 2);
                }
            }
            return false;
        }else if(type == InteractionType.SNEAK_CLICK) {
            if(TransportArmy.transports.containsKey(player)){
                new GUISession(player){
                    int cnt;
                    @Override
                    public void constructGUI(String guiID, GUI gui) {
                        switch (guiID) {
                            case "join-camped": {
                                int pos=10;
                                cnt = TransportArmy.transports.get(player).size();
                                for(TransportArmy i: TransportArmy.transports.get(player)){
                                    if(i.isActive&&i.getLocation().getBukkitLocation().distance(location.getBukkitLocation())<3){
                                        int p = pos;
                                        gui.addItem("item-"+pos, pos, i.stack.getItemDisplay())
                                                .setClickHandler((session, action, event) -> {
                                                    if(i.isActive) {
                                                        i.mob.remove();
                                                        stack.addArmyStack(i.stack);
                                                        i.unregister();
                                                        gui.removeItem(p);
                                                        hologram.setText(getHologramText());
                                                        cnt--;
                                                        if (cnt <= 0) {
                                                            player.getPlayer().closeInventory();
                                                        }
                                                    }
                                                });
                                        pos++;
                                    }
                                }
                                gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                            }
                        }
                    }
                }.display("join-camped");
            }
        }
        return true;
    }
}
