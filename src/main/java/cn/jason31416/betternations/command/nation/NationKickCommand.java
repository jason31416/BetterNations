package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.resolution.KickPlayerResolution;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NationKickCommand extends ChildCommand {
    public NationKickCommand(IParentCommand parent) {
        super(List.of("kick"), parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.checkArgs(ParameterType.PLAYER)) return null;
        Nation nation = context.getPlayer().getNation();
        if(nation==null){
            return Message.getMessage("command.failed.player-not-in-nation");
        }
        if(!context.getPlayer().getRank().hasPermission(Permission.KICK_PLAYER)){
            return Message.getMessage("command.failed.no-permission");
        }
        SimplePlayer target = context.getPlayerArg(0);
        if(target.getNation()!=nation) return Message.getMessage("command.failed.invalid-target");
        new KickPlayerResolution(nation, context.getPlayer(), target).propose();
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getPlayer().getNation()!=null) return context.getPlayer().getNation().getMembers().stream().map(SimplePlayer::getName).toList();
        return null;
    }
}
