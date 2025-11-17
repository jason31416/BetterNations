package cn.jason31416.betternations.command.admin;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.manager.UnclaimableRegionManager;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ToggleUnclaimableCommand extends ChildCommand {
    public ToggleUnclaimableCommand(IParentCommand parent) {
        super(List.of("unclaimable"), parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(!context.getSender().sender().isOp()) return Message.getMessage("command.failed.no-permission");
        if(context.getPlayer() == null) return null;
        if(UnclaimableRegionManager.unclaimableChunks.contains(context.getPlayer().getLocation().getChunkLocation())){
            UnclaimableRegionManager.unclaimableChunks.remove(context.getPlayer().getLocation().getChunkLocation());
            UnclaimableRegionManager.save();
            return Message.getMessage("command.success.unclaimable.disable");
        }else{
            UnclaimableRegionManager.unclaimableChunks.add(context.getPlayer().getLocation().getChunkLocation());
            UnclaimableRegionManager.save();
            return Message.getMessage("command.success.unclaimable.enable");
        }
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
