package cn.jason31416.betternations.army.states;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.mob.SimpleMob;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TransportArmy implements ArmyStackHolder {
    public static Map<SimplePlayer, Set<TransportArmy>> transports = new HashMap<>();
    public static Map<Entity, TransportArmy> transportArmyMap = new HashMap<>();
    public SimpleMob mob;
    public SimplePlayer player;
    public ArmyStack stack;
    public BukkitRunnable runnable;
    public boolean isActive=true;
    public TransportArmy(SimplePlayer player, SimpleMob mob, ArmyStack stack){
        stack.curHolder = this;
        this.stack = stack;
        this.mob = mob;
        this.player = player;
        if(!transports.containsKey(player)) transports.put(player, new HashSet<>());
        transports.get(player).add(this);
        transportArmyMap.put(mob.getBukkitEntity(), this);
        runnable = new BukkitRunnable() {
            double curPenalty=0.1;
            long lstwarn = 0;
            @Override
            public void run() {
                mob.setTarget(player.getPlayer());
                if(!(mob.getBukkitEntity().getVehicle() instanceof Boat) &&
                        (mob.getLocation().getBlockMaterial() == Material.WATER ||
                        mob.getLocation().getBlockMaterial() == Material.BUBBLE_COLUMN ||
                        (mob.getLocation().getBlock().getBlockData() instanceof Waterlogged a && a.isWaterlogged()))){
                    mob.damage(curPenalty);
                    curPenalty += 0.02;
                }else curPenalty = 0.1;
                if(player.getLocation().getBukkitLocation().distance(mob.getLocation().getBukkitLocation()) >= Config.getDouble("combat.transport-max-distance")){
                    destroy();
                }else if(player.getLocation().getBukkitLocation().distance(mob.getLocation().getBukkitLocation()) >= Config.getDouble("combat.transport-warn-distance")&
                       System.currentTimeMillis()-lstwarn>750){
                    Message.getMessage("combat.transport-too-far").send(player);
                    lstwarn = System.currentTimeMillis();
                }
            }
        };
        runnable.runTaskTimer(BetterNations.instance, 10, 0); // This updater checks for real-time actions
    }
    public void unregister(){
        transportArmyMap.remove(mob.getBukkitEntity());
        if(transports.containsKey(player)) {
            transports.get(player).remove(this);
            if (transports.get(player).isEmpty()) transports.remove(player);
        }
        runnable.cancel();
        isActive = false;
    }
    public void update(){ // This updater essentially checks for combat, etc.
        // todo: update transport army
    }
    public void destroy(boolean doKill){
        Message.getMessage("combat.transport-destroyed").send(player);
        unregister();
        if(doKill) mob.remove();
        stack.destroy();
    }
    public ArmyStack getStack() {
        return stack;
    }
    public void destroy(){
        destroy(true);
    }
    public SimpleLocation getLocation(){
        return mob.getLocation();
    }
    public static void spawn(SimpleLocation location, SimplePlayer player, ArmyStack stack){
        SimpleMob mob = SimpleMob.spawn(Config.getString("combat.transport-mob"), location, "");
        mob.setMaxHealth(stack.getHealth());
        mob.setHealth(stack.getHealth());
        TransportArmy army = new TransportArmy(
                player,
                mob,
                stack
        );
        army.updateName();
        stack.curHolder = army;
    }
    public void updateName(){
        mob.setName(Message.getMessage("combat.transport-name")
                .add("nation", stack.nation.getColorTag()+stack.nation.getName())
                .add("hp", Math.round(stack.getHealth()*10)/10.0)
                .add("mxhp", Math.round(stack.getMaxHealth()*10)/10.0)
                .add("units", stack.size()).toString());
    }
}
