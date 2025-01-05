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

public class TreatyRemovetermCommand extends ChildCommand {

    public TreatyRemovetermCommand(IParentCommand parent) {
        super("removeterm", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(!context.getSender().isPlayer()||!context.checkArgs(ParameterType.STRING, ParameterType.INTEGER)) return null;
        Treaty treaty = Treaty.treatyMap.get(context.getArg(0));
        if(treaty==null||!treaty.proposer.equals(context.getPlayer())) return Message.getMessage("command.failed.invalid-treaty");
        if(treaty.state!= Treaty.TreatyState.EDITING) return Message.getMessage("command.failed.treaty-not-editing");
        if(context.getIntArg(1)<1||context.getIntArg(1)>treaty.terms.size()) return Message.getMessage("command.failed.invalid-term-id");
        treaty.terms.remove(context.getIntArg(1)-1);
        if(context.getSender().isPlayer()) treaty.display(context.getSender().toPlayer());
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(!context.getArg(0).isEmpty()&&context.getCurrentArg()==1){
            return Treaty.treatyMap.keySet().stream().toList();
        }else if(context.getCurrentArg()==2){
            return List.of("<term id>");
        }
        return null;
    }
}
