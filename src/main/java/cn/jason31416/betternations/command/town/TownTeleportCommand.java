package cn.jason31416.betternations.command.town;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.nation.TownRole;
import cn.jason31416.betternations.structure.types.TownCore;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TownTeleportCommand extends ChildCommand {
    public TownTeleportCommand(IParentCommand parent) {
        super("tp", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.getSender().isPlayer()||!context.checkArgs(ParameterType.STRING)) return null;
        Town to = Town.getTown(context.getArg(0));
        if(to==null) return Message.getMessage("command.failed.town-not-exist");
        if(!to.getRole(context.getPlayer()).hasPermission(Permission.BUILD)) return Message.getMessage("command.failed.no-permission");
        new BukkitRunnable(){
            int countdown = Config.getInt("town.teleport-countdown", 3);
            final SimpleLocation loc = context.getPlayer().getLocation();
            @Override
            public void run() {
                if(!context.getPlayer().getLocation().equals(loc)){
                    Message.getMessage("town.teleport-canceled").send(context.getSender());
                    new StringMessage("").sendActionbar(context.getSender().toPlayer());
                    cancel();
                }
                Message.getMessage("town.teleport-countdown").add("timer", countdown).sendActionbar(context.getSender().toPlayer());
                if(countdown<=0){
                    if(context.getSender().toPlayer().getPlayer()!=null) {
                        context.getSender().toPlayer().getPlayer().teleport(to.core.location.getRelative(0, 1, 0).getBukkitLocation());
                        Message.getMessage("town.teleport-success").sendActionbar(context.getSender().toPlayer());
                    }
                    cancel();
                }
                countdown--;
            }
        }.runTaskTimer(BetterNations.instance, 0, 20);
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getPlayer().getNation()!=null){
            return context.getPlayer().getNation().getTowns().stream().map(Town::getName).toList();
        }
        return null;
    }
}
