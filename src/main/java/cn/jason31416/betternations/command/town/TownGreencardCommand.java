package cn.jason31416.betternations.command.town;

import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.nation.TownRole;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TownGreencardCommand extends ChildCommand {
    public TownGreencardCommand(IParentCommand parent) {
        super("greencard", parent);
    }

    @Override
    public @Nullable Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.checkArgs(ParameterType.PLAYER)) return null;
        Town town = context.getSender().toPlayer().getLocation().getChunkLocation().getTown();
        if(town == null) return Message.getMessage("command.failed.not-in-town");
        if(town.getRole(context.getPlayer())!=TownRole.MAYOR) return Message.getMessage("command.failed.no-permission");
        SimplePlayer player = context.getPlayerArg(0);
        if(town.getNation().getMembers().contains(player)||town.getRole(player)!=TownRole.NONE) return Message.getMessage("command.failed.player-in-nation");
        town.setRole(player, TownRole.GREENCARD);
        return Message.getMessage("command.success.granted-greencard")
                .add("player", player.getName())
                .add("town", town.getName());
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return Bukkit.getOnlinePlayers().stream().map(SimplePlayer::of).filter(p -> p.getNation()!=context.getPlayer().getNation()).map(SimplePlayer::getName).toList();
    }
}
