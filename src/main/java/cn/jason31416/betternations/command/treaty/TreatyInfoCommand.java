package cn.jason31416.betternations.command.treaty;

import cn.jason31416.betternations.nation.treaty.Treaty;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TreatyInfoCommand extends ChildCommand {

    public TreatyInfoCommand(IParentCommand parent) {
        super(List.of("info", "view"), parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(!context.getSender().isPlayer()||!context.checkArgs(ParameterType.STRING)) return null;
        if(!Treaty.treatyMap.containsKey(context.getArg(0))) return Message.getMessage("command.failed.invalid-treaty");
        Treaty.treatyMap.get(context.getArg(0)).display(context.getSender().toPlayer());
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(!context.getArg(0).isEmpty()){
            return Treaty.treatyMap.keySet().stream().toList();
        }
        return List.of("<treaty>");
    }
}
