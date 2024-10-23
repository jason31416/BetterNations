package cn.jason31416.planetlib.command.tempAction;

import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;

import javax.annotation.Nullable;
import java.util.List;

public class ExecuteActionCommand extends ChildCommand {
    public ExecuteActionCommand(IParentCommand parent) {
        super("act", parent);
    }

    @Override @Nullable
    public Message execute(ICommandContext context) {
        if(!context.checkArgs(ParameterType.STRING)) return null;
        String actionID = context.getArg(0);
        if(TempAction.actions.containsKey(actionID)){
            if(TempAction.actions.get(actionID).execute(context.player())) return null;
        }
        return StaticMessages.NO_API_COMMAND;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
