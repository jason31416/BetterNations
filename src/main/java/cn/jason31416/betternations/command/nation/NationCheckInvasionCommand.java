package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.betternations.army.states.ArmyCamp;
import cn.jason31416.betternations.army.states.InvasionFlag;
import cn.jason31416.betternations.army.states.SiegeFlag;
import cn.jason31416.betternations.army.states.StructuredArmy;
import cn.jason31416.betternations.manager.ArmyUpdateManager;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static cn.jason31416.planetlib.command.ParameterType.INTEGER;

public class NationCheckInvasionCommand extends ChildCommand {
    public NationCheckInvasionCommand(IParentCommand parent) {
        super("check", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        if(context.getPlayer().getNation()==null) return Message.getMessage("command.failed.player-not-in-nation");

        Bukkit.getScheduler().runTaskAsynchronously(BetterNations.instance, ()->{
            List<StructuredArmy> armyList = new ArrayList<>();
            for(SimpleChunkLocation chunk: new HashSet<>(ArmyCamp.armyLocationMap.keySet())) {
                for (StructuredArmy army : new HashSet<>(ArmyCamp.armyLocationMap.get(chunk))) {
                    if (army instanceof InvasionFlag i) {
                        if (i.getLocation().getChunkLocation().getNation()==context.getPlayer().getNation()){
                            armyList.add(i);
                        }
                    }else if(army instanceof SiegeFlag s){
                        if(s.target.getNation()==context.getPlayer().getNation()){
                            armyList.add(s);
                        }
                    }
                }
            }
            armyList.sort(Comparator.comparingInt(a -> -a.stack.size()));
            Message.getMessage("command.success.invasion-list-header").add("nation", context.getPlayer().getNation().getName()).send(context.getPlayer());
            if(armyList.isEmpty()) Message.getMessage("command.success.invasion-list-empty").send(context.getPlayer());
            for(StructuredArmy army: armyList){
                if(army instanceof InvasionFlag i){
                    Message.getMessage("command.success.invasion-list-invasion")
                            .add("nation", context.getPlayer().getNation().getName())
                            .add("attacking_nation", i.stack.nation.getName())
                            .add("count", i.stack.size())
                            .add("hp", Math.round(ArmyUpdateManager.chunkHealths.getOrDefault(i.getLocation().getChunkLocation(), 0d)*100)/100.0)
                            .add("x", i.getLocation().x())
                            .add("y", i.getLocation().y())
                            .add("z", i.getLocation().z())
                            .send(context.getPlayer());
                }else if(army instanceof SiegeFlag s){
                    Message.getMessage("command.success.invasion-list-siege")
                            .add("nation", context.getPlayer().getNation().getName())
                            .add("attacking_nation", s.stack.nation.getName())
                            .add("count", s.stack.size())
                            .add("target", s.target.getName())
                            .add("hp", Math.round((s.target.getHealth())*100)/100.0)
                            .add("x", s.getLocation().x())
                            .add("y", s.getLocation().y())
                            .add("z", s.getLocation().z())
                            .send(context.getPlayer());
                }
            }
        });

        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
