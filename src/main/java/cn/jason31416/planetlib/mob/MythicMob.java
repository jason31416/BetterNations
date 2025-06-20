package cn.jason31416.planetlib.mob;

import cn.jason31416.betternations.army.ArmyType;
import cn.jason31416.planetlib.hook.MythicMobsHook;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public class MythicMob implements SimpleMob {
    ActiveMob mob;
    public MythicMob(String name, String type, SimpleLocation location) {
        this.mob = MythicMobsHook.spawnMob(type, location, null);
        if(this.mob == null){
            throw new IllegalArgumentException("Invalid mob type: " + type);
        }
        this.mob.getEntity().setCustomName(name);
    }
    @Override
    public void teleport(SimpleLocation location) {
        mob.getEntity().teleport(BukkitAdapter.adapt(location.getBukkitLocation()));
    }
    @Override
    public SimpleLocation getLocation() {
        return SimpleLocation.of(BukkitAdapter.adapt(mob.getEntity().getLocation()));
    }

    @Override
    public boolean isAlive() {
        return !mob.getEntity().getBukkitEntity().isDead();
    }

    @Override
    public void damage(double hp) {
        mob.getEntity().damage((float) hp);
    }

    @Override
    public void setMaxHealth(double hp) {
        mob.getEntity().setMaxHealth((float) hp);
    }

    @Override
    public void setHealth(double hp){
        mob.getEntity().setHealth(hp);
        mob.getEntity().setMaxHealth(hp);
    }

    @Override
    public void setTarget(LivingEntity target) {
        mob.setTarget(BukkitAdapter.adapt(target));
    }

    @Override
    public Entity getBukkitEntity(){
        return mob.getEntity().getBukkitEntity();
    }

    @Override
    public void remove() {
        mob.remove();
    }

    @Override
    public void setName(String name) {
        mob.getEntity().setCustomName(name);
    }
}
