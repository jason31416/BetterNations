package cn.jason31416.planetlib.mob;

import cn.jason31416.betternations.army.ArmyType;
import cn.jason31416.planetlib.hook.MythicMobsHook;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.LivingEntity;

public class MythicMob implements SimpleMob {
    ActiveMob mob;
    public MythicMob(String name, String type, SimpleLocation location) {
        this.mob = MythicMobsHook.spawnMob(type, location, null);
        if(this.mob == null){
            throw new IllegalArgumentException("Invalid mob type: " + type);
        }
        this.mob.setDisplayName(name);
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
        return !mob.isDead();
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
    public void remove() {
        mob.remove();
    }
}
