package cn.jason31416.betternations.command.resolution;

import cn.jason31416.betternations.nation.resolution.AbstractResolution;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ResolutionListCommand extends ChildCommand {
    public ResolutionListCommand(IParentCommand parent) {
        super("list", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        SimplePlayer player = context.getPlayer();
        if(player == null){
            return null;
        }
        if(player.getNation()==null) return Message.getMessage("command.failed.player-not-in-nation");
        Message.getMessage("nation.resolution.list.header").add("nation", player.getNation().getName()).send(context.getSender());
        boolean bb=true;
        for(AbstractResolution i: new ArrayList<>(player.getNation().resolutions.values())){
            if(!i.checkDate()) player.getNation().resolutions.remove(i.resolutionId);
            else if(i.requiredSigners.contains(context.getPlayer())){
                bb = false;
                Message.getMessage("nation.resolution.list.item-with-permission")
                        .add("content", i.getResolutionContent())
                        .add("resolution_id", i.resolutionId)
                        .send(context.getSender());
            }else{
                bb = false;
                Message.getMessage("nation.resolution.list.item-no-permission")
                        .add("content", i.getResolutionContent())
                        .add("resolution_id", i.resolutionId)
                        .send(context.getSender());
            }
        }
        if(bb) Message.getMessage("nation.resolution.list.no-resolution").send(context.getSender());
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
