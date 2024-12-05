package cn.jason31416.betternations.command.admin;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReloadCommand extends ChildCommand {
    public ReloadCommand(IParentCommand parent) {
        super(List.of("reload"), parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(!context.getSender().sender().isOp()) return Message.getMessage("command.failed.no-permission");
        context.getSender().sendMessage(Message.getMessage("admin.reloading"));
        BetterNations.instance.reload();
        context.getSender().sendMessage(Message.getMessage("admin.reloaded"));
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
