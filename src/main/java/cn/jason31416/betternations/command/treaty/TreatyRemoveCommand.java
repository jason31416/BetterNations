
package cn.jason31416.betternations.command.treaty;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.treaty.AllyTerm;
import cn.jason31416.betternations.nation.treaty.PeaceTerm;
import cn.jason31416.betternations.nation.treaty.Treaty;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TreatyRemoveCommand extends ChildCommand {

    public TreatyRemoveCommand(IParentCommand parent) {
        super("remove", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(!context.getSender().isPlayer()) return null;
        Treaty treaty = Treaty.treatyMap.get(context.getArg(0));
        if(treaty==null||(!treaty.proposer.equals(context.getPlayer())&&!context.getPlayer().getPlayer().isOp())) return Message.getMessage("command.failed.invalid-treaty");
        if(treaty.state!= Treaty.TreatyState.EDITING) return Message.getMessage("command.failed.treaty-not-editing");
        Treaty.treatyMap.remove(context.getArg(0));
        return Message.getMessage("command.success.treaty-removed").add("treaty", treaty.name);
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(!context.getArg(0).isEmpty()&&context.getCurrentArg()==1){
            return Treaty.treatyMap.keySet().stream().toList();
        }
        return null;
    }
}
