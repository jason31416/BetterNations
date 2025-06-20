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
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.message.StringMessage;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TreatyAddtermCommand extends ChildCommand {

    public TreatyAddtermCommand(IParentCommand parent) {
        super("addterm", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(!context.getSender().isPlayer()||!context.checkArgs(ParameterType.STRING, ParameterType.STRING, ParameterType.NATION, ParameterType.NATION)) return null;
        Treaty treaty = Treaty.treatyMap.get(context.getArg(0));
        if(treaty==null||(!treaty.proposer.equals(context.getPlayer())&&!context.getPlayer().getPlayer().isOp())) return Message.getMessage("command.failed.invalid-treaty");
        if(treaty.state!= Treaty.TreatyState.EDITING) return Message.getMessage("command.failed.treaty-not-editing");
        if(context.getNationArg(2)==context.getNationArg(3)) return Message.getMessage("command.failed.invalid-term-arguments");
        switch (context.getArg(1)){
            case "peace": {
                treaty.terms.add(new PeaceTerm(context.getNationArg(2), context.getNationArg(3)));
                if(context.getSender().isPlayer()) treaty.display(context.getSender().toPlayer());
                break;
            }
            case "ally": {
                treaty.terms.add(new AllyTerm(context.getNationArg(2), context.getNationArg(3)));
                if(context.getSender().isPlayer()) treaty.display(context.getSender().toPlayer());
                break;
            }
            default: {
                Message.getMessage("command.failed.unknown-term-type").send(context.getSender());
            }
        }
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(!context.getArg(0).isEmpty()&&context.getCurrentArg()==1){
            return Treaty.treatyMap.keySet().stream().toList();
        }else if(context.getCurrentArg()==2){
            return List.of("peace", "ally");
        }else if(context.getCurrentArg()==3||context.getCurrentArg()==4){
            return Nation.nations.values().stream().map(Nation::getName).toList();
        }
        return null;
    }
}
