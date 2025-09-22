package cn.jason31416.betternations.mob;

import cn.jason31416.planetlib.hook.MythicMobsHook;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public interface SimpleMob {
    public static Set<SimpleMob> mobs = new HashSet<>();
    void teleport(SimpleLocation location);
    SimpleLocation getLocation();
    boolean isAlive();
    void damage(double hp);
    void setMaxHealth(double hp);
    void setTarget(LivingEntity target);
    void remove();
    void setName(String name);
    default void kill(){
        remove();
        mobs.remove(this);
    }
    void setHealth(double health);
    Entity getBukkitEntity();
    public static SimpleMob spawn(String type, SimpleLocation location, String name){
        SimpleMob mob;
        if(MythicMobsHook.loadedMobTypes().contains(type)){
            mob=new MythicMob(name, type, location);
        } else{
            try {
                mob = new VanillaMob(name, EntityType.valueOf(type.toUpperCase()), location);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }
        mobs.add(mob);
        return mob;
    }
}
