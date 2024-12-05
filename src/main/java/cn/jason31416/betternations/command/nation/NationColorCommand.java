package cn.jason31416.betternations.command.nation;

import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.bukkit.Color;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static cn.jason31416.planetlib.command.ParameterType.INTEGER;

public class NationColorCommand extends ChildCommand {
    public NationColorCommand(IParentCommand parent) {
        super("color", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.checkArgs(INTEGER, INTEGER, INTEGER)) return null;
        if(context.getPlayer().getNation()==null) return Message.getMessage("command.failed.player-not-in-nation");
        if(context.getPlayer().getRank().weight()<500) return Message.getMessage("command.failed.no-permission");
        context.getPlayer().getNation().setColor(Color.fromRGB(context.getIntArg(0), context.getIntArg(1), context.getIntArg(2)));

        return Message.getMessage("command.success.color-changed");
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg()==1) return List.of("<red(0-255)>");
        if(context.getCurrentArg()==2) return List.of("<green(0-255)>");
        if(context.getCurrentArg()==3) return List.of("<blue(0-255)>");
        return null;
    }
}
