package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.nation.resolution.RenameResolution;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NationRenameCommand extends ChildCommand {
    public NationRenameCommand(IParentCommand parent) {
        super("rename", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||context.getPlayer().getNation()==null) return Message.getMessage("command.failed.player-not-in-nation");
        if(!context.checkArgs(ParameterType.STRING)) return null;
        new RenameResolution(context.getPlayer().getNation(), context.getPlayer(), context.getArg(0)).propose();
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
