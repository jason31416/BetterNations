package cn.jason31416.betternations.command.town;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.nation.TownRole;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TownRenameCommand extends ChildCommand {
    public TownRenameCommand(IParentCommand parent) {
        super("rename", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.getSender().isPlayer()||!context.checkArgs(ParameterType.STRING)) return null;
        String newName = context.getArg(0);
        if(Town.getTown(newName)!=null) return Message.getMessage("command.failed.town-name-exists");
        Town town = context.getSender().toPlayer().getLocation().getChunkLocation().getTown();
        if(town == null) return Message.getMessage("command.failed.not-in-town");
        if(town.getRole(context.getPlayer()) != TownRole.MAYOR) return Message.getMessage("command.failed.no-permission");
        town.rename(newName);
        return Message.getMessage("command.success.renamed-town");
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return List.of("<name>");
    }
}
