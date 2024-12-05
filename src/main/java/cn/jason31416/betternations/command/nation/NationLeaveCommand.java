package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NationLeaveCommand extends ChildCommand {
    public NationLeaveCommand(IParentCommand parent) {
        super(List.of("leave", "quit"), parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        Nation nation = context.getPlayer().getNation();
        if(nation==null){
            return Message.getMessage("command.failed.player-not-in-nation");
        }
        if(nation.getOwner()==context.getPlayer()){
            return Message.getMessage("command.failed.owner-cannot-leave");
        }
        nation.removePlayer(context.getPlayer());
        return Message.getMessage("command.success.left-nation").add("nation", nation.getName());
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
