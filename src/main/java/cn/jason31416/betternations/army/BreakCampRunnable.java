package cn.jason31416.betternations.army;

import cn.jason31416.betternations.army.states.StructuredArmy;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.mob.SimpleMob;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BreakCampRunnable extends BukkitRunnable {
    public static Map<SimplePlayer, BreakCampRunnable> breakingPlayers = new HashMap<>();
    public static HashSet<Entity> defendingMobs = new HashSet<>();
    public static class CampBreakingListener implements Listener {
        @EventHandler
        public void onMobDeath(EntityDeathEvent event){
            if(defendingMobs.contains(event.getEntity())){
                event.setDroppedExp(0);
                event.getDrops().clear();
                defendingMobs.remove(event.getEntity());
            }
        }
        @EventHandler
        public void onMobHeal(EntityRegainHealthEvent event){
            if(defendingMobs.contains(event.getEntity())){
                event.setCancelled(true);
            }
        }
        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event){
            SimplePlayer player = SimplePlayer.of(event.getPlayer());
            if(event.getTo()==null) return;
            if(breakingPlayers.containsKey(player)&&!breakingPlayers.get(player).camp.location.getChunkLocation().equals(SimpleLocation.of(event.getTo()).getChunkLocation())){
                breakingPlayers.get(player).failed();
            }
        }
        @EventHandler
        public void onPlayerDeath(PlayerDeathEvent event){
            SimplePlayer player = SimplePlayer.of(event.getEntity());
            if(breakingPlayers.containsKey(player)){
                breakingPlayers.get(player).failed();
            }
        }
        @EventHandler
        public void onPlayeQuit(PlayerQuitEvent event){
            SimplePlayer player = SimplePlayer.of(event.getPlayer());
            if(breakingPlayers.containsKey(player)){
                breakingPlayers.get(player).failed();
            }
        }
        @EventHandler
        public void onMobLeashed(PlayerLeashEntityEvent event){
            if(defendingMobs.contains(event.getEntity())) event.setCancelled(true);
        }
    }
    public SimplePlayer breaker;
    public StructuredArmy camp;
    public Map<SimpleMob, ArmyType> instanceMobs=new HashMap<>();
    public BreakCampRunnable(SimplePlayer breaker, StructuredArmy camp){
        this.breaker = breaker;
        this.camp = camp;
        breakingPlayers.put(breaker, this);
    }
    public ArmyType getNextType(){
        if(camp.stack.armies.isEmpty()) return null;
        int rnd = new Random().nextInt()%camp.stack.size();
        for(ArmyType i: camp.stack.armies.keySet()){
            rnd -= camp.stack.armies.get(i).count;
            if(rnd<0) return i;
        }
        return null;
    }
    public void failed(){
        Message.getMessage("combat.breakage.failed").send(breaker);
        breakingPlayers.remove(breaker);
        camp.runnable = null;
        for(SimpleMob i: instanceMobs.keySet()){
            defendingMobs.remove(i.getBukkitEntity());
            if(i.isAlive()) {
                camp.stack.addArmy(instanceMobs.get(i), 1, ((Mob) i.getBukkitEntity()).getHealth());
                i.remove();
            }
        }
        camp.hologram.setText(camp.getHologramText());
        camp.stack.processDamageQueue(); // to process all of the damages accumulated during the time
        cancel();
    }
    @Override
    public void run() {
        if(!camp.running){
            cancel();
            return;
        }
        while(instanceMobs.size() < Config.getInt("combat.defend-units", 10)){
            ArmyType type = getNextType();
            if(type==null) break;
            System.out.println(Message.getMessage("combat.defender-name").add("nation", camp.stack.nation.getColorTag()+camp.stack.nation.getName()).add("type", type.name).toString());
            SimpleMob mob = SimpleMob.spawn(type.type, breaker.getLocation(), Message.getMessage("combat.defender-name").add("nation", camp.stack.nation.getColorTag()+camp.stack.nation.getName()).add("type", type.name).toString());
            double hp = camp.stack.armies.get(type).hp/camp.stack.armies.get(type).count;
            mob.setMaxHealth(hp);
            mob.setHealth(hp);
            camp.stack.armies.get(type).hp -= hp;
            camp.stack.armies.get(type).count -= 1;
            if(camp.stack.armies.get(type).count<=0){
                camp.stack.armies.remove(type);
            }
            instanceMobs.put(mob, type);
            defendingMobs.add(mob.getBukkitEntity());
        }
        if(instanceMobs.isEmpty()){
            Message.getMessage("combat.breakage.success").send(breaker);
            breakingPlayers.remove(breaker);
            camp.breakStructure();
            camp.unregister();
            cancel();
            return;
        }
        for(SimpleMob i: new ArrayList<>(instanceMobs.keySet())){
            if(!i.isAlive()){
                defendingMobs.remove(i.getBukkitEntity());
                instanceMobs.remove(i);
                continue;
            }
            if(!i.getLocation().getChunkLocation().equals(camp.location.getChunkLocation())){
                i.teleport(breaker.getLocation());
            }
        }
    }
}
