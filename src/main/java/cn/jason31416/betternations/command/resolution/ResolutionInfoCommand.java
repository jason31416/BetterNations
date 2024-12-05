package cn.jason31416.betternations.command.resolution;

import cn.jason31416.betternations.nation.resolution.AbstractResolution;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static cn.jason31416.planetlib.command.ParameterType.STRING;

public class ResolutionInfoCommand extends ChildCommand {
    public ResolutionInfoCommand(IParentCommand parent) {
        super("info", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        SimplePlayer player = context.getPlayer();
        if(player == null||!context.checkArgs(STRING)){
            return null;
        }
        if(player.getNation() == null){
            return Message.getMessage("command.failed.player-not-in-nation");
        }
        String id = context.getArg(0);
        AbstractResolution resolution = player.getNation().resolutions.get(id);
        if(resolution == null){
            return Message.getMessage("command.failed.resolution-not-found");
        }
        Message message;
        if(resolution.requiredSigners.contains(player)) message = MessageLoader.getList("nation.resolution.info")
                .add("proposer", resolution.proposer.getName())
                .add("nation", resolution.nation.getName())
                .add("resolution_id", resolution.resolutionId)
                .add("content", resolution.getResolutionContent().toFormatted())
                .add("signed_players", String.join(",", resolution.signedPlayers.stream().map(SimplePlayer::getName).toList()))
                .add("signed_players_count", ""+resolution.signedPlayers.size())
                .add("required_signers", (int)Math.max(Math.ceil(resolution.requiredSigners.size()*resolution.requiredRatio), resolution.minimalSigners));
        else
            message = MessageLoader.getList("nation.resolution.info-no-permission")
                    .add("proposer", resolution.proposer.getName())
                    .add("nation", resolution.nation.getName())
                    .add("resolution_id", resolution.resolutionId)
                    .add("content", resolution.getResolutionContent().toFormatted())
                    .add("signed_players", String.join(",", resolution.signedPlayers.stream().map(SimplePlayer::getName).toList()))
                    .add("signed_players_count", ""+resolution.signedPlayers.size())
                    .add("required_signers", (int)Math.max(Math.ceil(resolution.requiredSigners.size()*resolution.requiredRatio), Math.min(resolution.minimalSigners, resolution.requiredSigners.size())));
        message.send(player);
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return List.of("<id>");
    }
}
