package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.resolution.JoinResolution;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NationJoinCommand extends ChildCommand {
    public NationJoinCommand(IParentCommand parent) {
        super("join", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.checkArgs(ParameterType.STRING)) return null;
        Nation nation = Nation.getNation(context.getArg(0));
        if(context.getPlayer().getNation()!=null){
            return Message.getMessage("command.failed.player-already-in-nation");
        }
        if(nation==null) return Message.getMessage("command.failed.nation-doesnt-exist");
        new JoinResolution(nation, context.getPlayer()).propose();
        return Message.getMessage("command.success.sent-join-request").add("nation", nation.getName());
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg()==1) return Nation.nations.values().stream().map(Nation::getName).toList();
        return null;
    }
}
