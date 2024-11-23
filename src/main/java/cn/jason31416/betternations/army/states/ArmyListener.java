package cn.jason31416.betternations.army.states;

import cn.jason31416.betternations.BetterNations;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ArmyListener implements Listener {
    @EventHandler
    public void onTransportDeath(EntityDeathEvent event){
        if(TransportArmy.transportArmyMap.containsKey(event.getEntity())){
            TransportArmy.transportArmyMap.get(event.getEntity()).destroy(false);
        }
    }
    @EventHandler
    public void onTransportDamaged(EntityDamageEvent event){
        if(TransportArmy.transportArmyMap.containsKey(event.getEntity()) && event.getEntity() instanceof Mob mob && mob.getHealth()>event.getFinalDamage()){
            TransportArmy.transportArmyMap.get(event.getEntity()).stack.damage(event.getFinalDamage());
            new BukkitRunnable(){
                @Override
                public void run() {
                    mob.setMaxHealth(mob.getHealth());
                }
            }.runTaskLater(BetterNations.instance, 0);
        }
    }
    @EventHandler
    public void onTransportDamagePlayer(EntityDamageByEntityEvent event){
        if(TransportArmy.transportArmyMap.containsKey(event.getDamager()) && event.getEntity() instanceof Player){
            event.setCancelled(true);
        }
    }
}
