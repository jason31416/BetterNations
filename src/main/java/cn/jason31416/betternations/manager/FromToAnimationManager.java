package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.update.UpdateCycle;
import org.bukkit.Color;

import java.util.HashSet;
import java.util.Set;

public class FromToAnimationManager {
    public static Set<FromToAnimationManager> managers=new HashSet<>();
    public Set<FromToParticleAnimation> particles=new HashSet<>();
    public AbstractStructure from, to;
    public Color color;
    public FromToAnimationManager(AbstractStructure from, AbstractStructure to, Color color){
        this.from = from;
        this.to = to;
        this.color = color;
        managers.add(this);
    }
    public void unregister(){
        managers.remove(this);
    }
    public static void updateAll(){
        for(FromToAnimationManager i: new HashSet<>(managers)){
            if(!i.from.exists||!i.to.exists){
                i.unregister();
                continue;
            }
            if(UpdateCycle.instance.tick% Config.getInt("combat.particle-frequency")==0){
                i.particles.add(new FromToParticleAnimation(i.from.location.getRelative(0.5, 0.5, 0.5), i.to.location.getRelative(0.5, 0.5, 0.5), i.color));
            }
            for(FromToParticleAnimation j: new HashSet<>(i.particles)){
                if(!j.display()) i.particles.remove(j);
            }
        }
    }
}
