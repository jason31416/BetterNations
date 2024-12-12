package cn.jason31416.betternations.army.states;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityMountEvent;

public class ArmyListener implements Listener {
    @EventHandler
    public void onTransportDeath(EntityDeathEvent event){
        if(TransportArmy.transportArmyMap.containsKey(event.getEntity())){
            TransportArmy.transportArmyMap.get(event.getEntity()).destroy(false);
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }
    @EventHandler
    public void onTransportDamaged(EntityDamageEvent event){
        if(TransportArmy.transportArmyMap.containsKey(event.getEntity()) && event.getEntity() instanceof Mob mob && mob.getHealth()>event.getFinalDamage()){
            TransportArmy.transportArmyMap.get(event.getEntity()).stack.damage(event.getFinalDamage());
            TransportArmy.transportArmyMap.get(event.getEntity()).updateName();
            new BukkitRunnable(){
                @Override
                public void run() {
                    mob.setMaxHealth(mob.getHealth());
                }
            }.runTaskLater(BetterNations.instance, 0);
        }
    }
    @EventHandler
    public void onTransportHeal(EntityRegainHealthEvent event){
        if(TransportArmy.transportArmyMap.containsKey(event.getEntity())){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onTransportLeashed(PlayerLeashEntityEvent event){
        if(TransportArmy.transportArmyMap.containsKey(event.getEntity())){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onTransportMount(EntityMountEvent event){
        if(TransportArmy.transportArmyMap.containsKey(event.getEntity())
            &&!((event.getMount() instanceof RideableMinecart)||(event.getMount() instanceof Boat))){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onTransportDamagePlayer(EntityDamageByEntityEvent event){
        if(TransportArmy.transportArmyMap.containsKey(event.getDamager()) && event.getEntity() instanceof Player){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractTransport(PlayerInteractEntityEvent event){
        if(TransportArmy.transportArmyMap.containsKey(event.getRightClicked())){
            SimplePlayer player = SimplePlayer.of(event.getPlayer());
            TransportArmy army = TransportArmy.transportArmyMap.get(event.getRightClicked());
            if(player.getNation()==army.stack.nation&&player.hasPermission(Permission.MANAGE_ARMY)) new GUISession(player){

                @Override
                public void constructGUI(String guiID, GUI gui) {
                    switch (guiID) {
                        case "transport-army": {
                            army.stack.displayGUI(gui, 10, 44);
                            gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                            gui.getItems("army-overview").setItemStack(army.stack.getItemDisplay());
                            gui.getItems("action-page").setClickHandler(new GUI.SwitchGuiRunnable("transport-actions"));
                        }
                        case "transport-actions": {
                            gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                            gui.getItems("encamp").setClickHandler((session, action, evt) -> {
                                if(army.isActive){
                                    SimpleLocation loc = army.mob.getLocation().getBlockLocation();
                                    while(loc.y()<loc.world().getBukkitWorld().getMaxHeight()&&loc.getBlockMaterial()!=Material.AIR){
                                        loc = loc.getRelative(0, 1, 0);
                                    }
                                    if(loc.y()>=loc.world().getBukkitWorld().getMaxHeight()) return;
                                    ArmyCamp c = new ArmyCamp();
                                    army.unregister();
                                    c.stack = army.stack;
                                    c.location = loc;
                                    army.stack.curHolder = c;
                                    c.place();
                                    army.mob.remove();
                                }
                            });
                            if(army.getLocation().getChunkLocation().isClaimed()&&!army.getLocation().getChunkLocation().isTownChunk()&&army.stack.nation.getRelation(army.getLocation().getChunkLocation().getNation())== Relation.ENEMY){
                                gui.getItems("invade").setClickHandler((session, action, evt) -> {
                                    if(army.isActive){
                                        if(army.getLocation().getChunkLocation().isClaimed()&&!army.getLocation().getChunkLocation().isTownChunk()&&army.stack.nation.getRelation(army.getLocation().getChunkLocation().getNation())== Relation.ENEMY) {
                                            SimpleLocation loc = army.mob.getLocation().getBlockLocation();
                                            while(loc.y()<loc.world().getBukkitWorld().getMaxHeight()&&loc.getBlockMaterial()!=Material.AIR){
                                                loc = loc.getRelative(0, 1, 0);
                                            }
                                            if(loc.y()>=loc.world().getBukkitWorld().getMaxHeight()) return;
                                            InvasionFlag c = new InvasionFlag();
                                            army.unregister();
                                            c.stack = army.stack;
                                            c.location = loc;
                                            army.stack.curHolder = c;
                                            c.place();
                                            army.mob.remove();
                                            player.getPlayer().closeInventory();
                                        }
                                    }
                                });
                            }else gui.getItems("invade").setMaterial(Material.AIR);
                            Town adjTown=null;
                            if(army.getLocation().getChunkLocation().isClaimed()&&army.getLocation().getChunkLocation().getNation()==army.stack.nation) for(SimpleChunkLocation i: army.getLocation().getChunkLocation().getAdjacentChunks()){
                                if(i.isTownChunk()&&army.stack.nation.getRelation(i.getNation())==Relation.ENEMY){
                                    adjTown = i.getTown();
                                    break;
                                }
                            }
                            if(adjTown!=null){
                                Town t = adjTown;
                                gui.getItems("siege")
                                        .placeholder("town", adjTown.getName())
                                        .setClickHandler((session, action, evt) -> {
                                            StaticMessages.debug(t.getName()+","+army.stack.nation.getRelation(t.getNation())+","+army.stack.nation.getName());
                                    if(army.isActive){
                                        if(army.stack.nation.getRelation(t.getNation())== Relation.ENEMY) {
                                            SimpleLocation loc = army.mob.getLocation().getBlockLocation();
                                            while(loc.y()<loc.world().getBukkitWorld().getMaxHeight()&&loc.getBlockMaterial()!=Material.AIR){
                                                loc = loc.getRelative(0, 1, 0);
                                            }
                                            if(loc.y()>=loc.world().getBukkitWorld().getMaxHeight()) return;
                                            SiegeFlag c = new SiegeFlag(t);
                                            army.unregister();
                                            c.stack = army.stack;
                                            c.location = loc;
                                            army.stack.curHolder = c;
                                            c.place();
                                            army.mob.remove();
                                            player.getPlayer().closeInventory();
                                        }
                                    }
                                });
                            }else gui.getItems("siege").setMaterial(Material.AIR);
                        }
                    }
                }
            }.display("transport-army");
        }
    }
}
