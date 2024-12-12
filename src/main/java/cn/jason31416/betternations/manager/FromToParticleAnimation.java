package cn.jason31416.betternations.manager;

import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class FromToParticleAnimation {
    public SimpleLocation loc, target;
    public Color color;
    public FromToParticleAnimation(SimpleLocation loc, SimpleLocation target, Color color){
        this.loc = loc;
        this.target = target;
        this.color = color;
    }
    public boolean display(){
        double speed = Config.getDouble("combat.particle-speed");
        if(loc.getBukkitLocation().distance(target.getBukkitLocation())<=speed){
            return false;
        }
        Vector movement = target.getBukkitLocation().toVector().subtract(loc.getBukkitLocation().toVector()).normalize().multiply(speed);
        loc = loc.getRelative(movement.getX(), movement.getY(), movement.getZ());
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, (float) 1.5);
        for(Player i: Bukkit.getOnlinePlayers()){
            if(i.getLocation().distance(loc.getBukkitLocation())<=32){
                i.spawnParticle(Particle.REDSTONE, loc.getBukkitLocation(), 1, dustOptions);
            }
        }
        return true;
    }
}
