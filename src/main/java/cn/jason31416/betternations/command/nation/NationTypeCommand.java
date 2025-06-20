package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.nation.NationType;
import cn.jason31416.betternations.nation.resolution.ChangeTypeResolution;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

import static cn.jason31416.planetlib.command.ParameterType.STRING;

public class NationTypeCommand extends ChildCommand {
    public NationTypeCommand(IParentCommand parent) {
        super("changetype", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.checkArgs(STRING)) return null;
        if(context.getPlayer().getNation()==null) return Message.getMessage("command.failed.player-not-in-nation");
        if(!List.of("democracy", "republic", "monarchy", "autocracy", "anarchy").contains(context.getArg(0).toLowerCase())) return Message.getMessage("command.failed.invalid-nation-type");
        new ChangeTypeResolution(context.getPlayer().getNation(), context.getPlayer(), NationType.valueOf(context.getArg(0).toUpperCase())).propose();
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg()==1) return List.of("democracy", "republic", "monarchy", "autocracy", "anarchy");
        return null;
    }
}
