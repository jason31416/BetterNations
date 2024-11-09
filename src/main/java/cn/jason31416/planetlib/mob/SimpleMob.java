package cn.jason31416.planetlib.mob;

import cn.jason31416.planetlib.hook.MythicMobsHook;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface SimpleMob {
    public static Set<SimpleMob> mobs = new HashSet<>();
    void teleport(SimpleLocation location);
    SimpleLocation getLocation();
    boolean isAlive();
    void setTarget(LivingEntity target);
    void remove();
    default void kill(){
        remove();
        mobs.remove(this);
    }
    void setHealth(double health);
    public static SimpleMob spawn(String type, SimpleLocation location, String name){
        SimpleMob mob;
        if(MythicMobsHook.loadedMobTypes().contains(type)){
            mob=new MythicMob(name, type, location);
        } else{
            mob=new VanillaMob(name, EntityType.valueOf(type), location);
        }
        mobs.add(mob);
        return mob;
    }
}
