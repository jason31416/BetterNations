package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.resolution.DisbandResolution;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NationDisbandCommand extends ChildCommand {

    public NationDisbandCommand(IParentCommand parent) {
        super("disband", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        Nation nation = context.getPlayer().getNation();
        if(nation==null) return Message.getMessage("command.failed.player-not-in-nation");
        new DisbandResolution(nation, context.getPlayer()).propose();
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
