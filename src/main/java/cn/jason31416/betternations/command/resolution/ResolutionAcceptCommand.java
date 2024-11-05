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

public class ResolutionAcceptCommand extends ChildCommand {
    public ResolutionAcceptCommand(IParentCommand parent) {
        super("sign", parent);
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
        if(!resolution.canSign(player)){
            return Message.getMessage("command.failed.resolution-not-signable");
        }
        resolution.sign(player);
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return List.of("<id>");
    }
}
