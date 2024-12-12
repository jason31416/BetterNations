package cn.jason31416.betternations.structure;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Hologram {
    public static Map<SimpleChunkLocation, Set<Hologram>> holograms = new HashMap<>();
    public SimpleLocation location;
    public ArmorStand textDisplay=null;
    public String currentText;
    public Hologram(SimpleLocation location, String text) {
        this.location = location;
        if(!holograms.containsKey(location.getChunkLocation())) holograms.put(location.getChunkLocation(), new HashSet<>());
        holograms.get(location.getChunkLocation()).add(this);
        currentText=text;
        spawn();
    }
    public void spawn(){
        if(location.getChunkLocation().getChunk().isEntitiesLoaded()&&textDisplay==null){
            World world = location.getBukkitLocation().getWorld();
            if(world == null) return;
            textDisplay = (ArmorStand) world.spawnEntity(location.getBukkitLocation(), EntityType.ARMOR_STAND);
            textDisplay.setVisible(false);
            textDisplay.setCustomName(currentText);
            textDisplay.setCustomNameVisible(true);
            textDisplay.setGravity(false);
            textDisplay.setMarker(true);
            textDisplay.setCollidable(false);
        }
    }
    public void despawn(){
        if(location.getChunkLocation().getChunk().isEntitiesLoaded()&&textDisplay!=null){
            textDisplay.remove();
            textDisplay = null;
        }
    }
    public static Hologram createHologram(@Nonnull SimpleLocation location, @Nonnull String text){
        return new Hologram(location, text);
    }
    public void setText(String text) {
        currentText = text;
        if(textDisplay!=null) textDisplay.setCustomName(text);
    }
    public void removeHologram() {
        if(holograms.containsKey(location.getChunkLocation())){
            holograms.get(location.getChunkLocation()).remove(this);
            if(holograms.get(location.getChunkLocation()).isEmpty()) holograms.remove(location.getChunkLocation());
        }
        despawn();
    }
}
