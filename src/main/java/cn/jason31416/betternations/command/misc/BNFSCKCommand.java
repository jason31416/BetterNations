package cn.jason31416.betternations.command.misc;

import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BNFSCKCommand extends ChildCommand {
    public BNFSCKCommand(IParentCommand parent) {
        super("fsck", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        return Message.getMessage("command.success.checked-holograms");
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
