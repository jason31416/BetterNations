package cn.jason31416.betternations.command.treaty;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.treaty.Treaty;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TreatySubmitCommand extends ChildCommand {

    public TreatySubmitCommand(IParentCommand parent) {
        super("submit", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(!context.getSender().isPlayer()) return null;
        Treaty treaty = Treaty.treatyMap.get(context.getArg(0));
        if(treaty==null||!treaty.proposer.equals(context.getPlayer())) return Message.getMessage("command.failed.invalid-treaty");
        if(treaty.terms.isEmpty()) return Message.getMessage("command.failed.cannot-propose-empty-treaty");
        if(treaty.state!= Treaty.TreatyState.EDITING) return Message.getMessage("command.failed.treaty-not-editing");
        treaty.submit();
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
