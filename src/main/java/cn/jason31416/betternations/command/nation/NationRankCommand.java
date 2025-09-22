package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.NationalRank;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.resolution.PlayerRankResolution;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NationRankCommand extends ChildCommand {
    public NationRankCommand(IParentCommand parent) {
        super("rank", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.checkArgs(ParameterType.STRING, ParameterType.STRING)) return null;
        Nation nation = context.getPlayer().getNation();
        if(nation==null){
            return Message.getMessage("command.failed.player-not-in-nation");
        }
        SimplePlayer player = SimplePlayer.of(context.getArg(0));
        NationalRank rank = NationalRank.getRank(context.getArg(1));
        if(rank==null||!nation.getType().allRanks.contains(rank)){
            return Message.getMessage("command.failed.invalid-rank");
        }
        if(player.getNation()!=nation) return Message.getMessage("command.failed.invalid-target");
        if(!context.getPlayer().getRank().hasPermission(Permission.CHANGE_RANK)||!context.getPlayer().getRank().isHigher(player.getRank())){
            return Message.getMessage("command.failed.no-permission");
        }
        if(context.getPlayer().getRank()==nation.getType().getOwnerRank()&&rank==nation.getType().getOwnerRank()){
            nation.setRank(nation.getOwner(), nation.getType().getDefaultRank());
            nation.setOwner(player);
            return Message.getMessage("command.success.owner-change").add("player", player.getName());
        }
        new PlayerRankResolution(nation, context.getPlayer(), player, rank).propose();
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg() == 1) {
            Nation nation = context.getPlayer().getNation();
            if(nation==null) return null;
            return nation.getMembers().stream().map(SimplePlayer::getName).toList();
        }else if(context.getCurrentArg()==2) {
            Nation nation = context.getPlayer().getNation();
            if(nation==null) return null;
            return nation.getType().allRanks.stream().map(NationalRank::name).toList();
        }
        return null;
    }
}
