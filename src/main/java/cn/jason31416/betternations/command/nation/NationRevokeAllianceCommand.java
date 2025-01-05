package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.nation.resolution.DeclareWarResolution;
import cn.jason31416.betternations.nation.resolution.RevokeAllianceResolution;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NationRevokeAllianceCommand extends ChildCommand {
    public NationRevokeAllianceCommand(IParentCommand parent) {
        super(List.of("revoke"), parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.checkArgs(ParameterType.NATION)) return null;
        Nation nation = context.getPlayer().getNation();
        if(nation==null){
            return Message.getMessage("command.failed.player-not-in-nation");
        }
        Nation target = context.getNationArg(0);
        if(nation==target) return Message.getMessage("command.failed.cannot-change-self-relation");
        if(nation.getRelation(target)!=Relation.ALLY) return Message.getMessage("command.failed.not-allied");
        new RevokeAllianceResolution(nation, context.getPlayer(), target).propose();
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg() == 1) {
            return Nation.nations.values().stream().map(Nation::getName).toList();
        }
        return null;
    }
}
