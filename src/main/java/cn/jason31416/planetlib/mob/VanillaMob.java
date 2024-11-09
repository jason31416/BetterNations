package cn.jason31416.planetlib.mob;

import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class VanillaMob implements SimpleMob{
    Mob entity;

    public VanillaMob(String name, EntityType type, SimpleLocation location) {
        if(location.getBukkitLocation().getWorld()==null){
            throw new IllegalArgumentException("Location must have a world");
        }
        entity = (Mob) location.getBukkitLocation().getWorld().spawnEntity(location.getBukkitLocation(), type);
        entity.setCustomName(name);
        entity.setCustomNameVisible(true);
        if(entity instanceof Ageable ageableEntity){
            ageableEntity.setAdult();
        }
        EntityEquipment equipment = entity.getEquipment();
        if(equipment!= null) {
            equipment.setItemInMainHand(null);
            equipment.setItemInOffHand(null);
            equipment.setHelmet(new ItemStack(Material.IRON_HELMET));
            equipment.setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            equipment.setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            equipment.setBoots(new ItemStack(Material.IRON_BOOTS));
        }
        entity.teleport(location.getBukkitLocation());
    }
    @Override
    public void teleport(SimpleLocation location) {
        entity.teleport(location.getBukkitLocation());
    }
    @Override
    public SimpleLocation getLocation() {
        return SimpleLocation.of(entity.getLocation());
    }
    @Override
    public boolean isAlive() {
        return !entity.isDead();
    }
    @Override
    public void setHealth(double hp){
        entity.setHealth(hp);
        entity.setMaxHealth(hp);
    }
    @Override
    public void setTarget(LivingEntity target) {
        entity.setTarget(target);
    }
    @Override
    public void remove() {
        entity.remove();
    }
}
