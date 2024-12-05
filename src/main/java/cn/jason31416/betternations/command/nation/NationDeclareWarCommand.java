package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.NationalRank;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.nation.resolution.DeclareWarResolution;
import cn.jason31416.betternations.nation.resolution.PlayerRankResolution;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NationDeclareWarCommand extends ChildCommand {
    public NationDeclareWarCommand(IParentCommand parent) {
        super(List.of("enemy", "declarewar"), parent);
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
        if(nation.getRelation(target)==Relation.ENEMY) return Message.getMessage("command.failed.already-enemy");
        new DeclareWarResolution(nation, context.getPlayer(), target).propose();
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
