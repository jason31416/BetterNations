package cn.jason31416.betternations.structure;

import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class Hologram {
    public static Map<SimpleLocation, Hologram> holograms = new HashMap<>();
    public SimpleLocation location;
    public ArmorStand textDisplay;
    public Hologram(SimpleLocation location, String text) {
        this.location = location;
        holograms.put(location, this);
        World world = location.getBukkitLocation().getWorld();
        if(world == null) return;
        textDisplay = (ArmorStand) world.spawnEntity(location.getBukkitLocation(), EntityType.ARMOR_STAND);
        textDisplay.setVisible(false);
        textDisplay.setCustomName(text);
        textDisplay.setCustomNameVisible(true);
        textDisplay.setGravity(false);
        textDisplay.setMarker(true);
        textDisplay.setCollidable(false);
        NbtHook.addTag(textDisplay, "bk.hologram");
    }
    public static int checkHolograms() {
        int count = 0;
        for(World world : Bukkit.getWorlds()){
            for(Entity entity : world.getEntities()){
                if(entity instanceof ArmorStand && NbtHook.hasTag(entity, "bk.hologram")){
                    SimpleLocation location = SimpleLocation.of(entity.getLocation());
                    if(!holograms.containsKey(location)){
                        entity.remove();
                        count++;
                    }
                }
            }
        }
        return count;
    }
    public static Hologram createHologram(@Nonnull SimpleLocation location, @Nonnull String text){
        return new Hologram(location, text);
    }
    public void setText(String text) {
        textDisplay.setCustomName(text);
    }
    public void removeHologram() {
        holograms.remove(location);
        textDisplay.remove();
    }
}
