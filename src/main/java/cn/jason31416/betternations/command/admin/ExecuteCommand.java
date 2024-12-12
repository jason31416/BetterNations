package cn.jason31416.betternations.command.admin;

import cn.jason31416.betternations.command.BetterNationsCommand;
import cn.jason31416.planetlib.command.*;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExecuteCommand extends ChildCommand {
    public ExecuteCommand(IParentCommand parent) {
        super("execute", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(!context.getSender().sender().isOp()) return Message.getMessage("command.failed.no-permission");
        if(!context.checkArgs(ParameterType.STRING, ParameterType.STRING)) return null;
        SimplePlayer target=context.getPlayerArg(0);
        if(target==null) return Message.getMessage("command.failed.invalid-target");
        ICommandContext ctx = new CommandContext(context.args().subList(2, context.args().size()), context.getSender(), target, "k admin execute");
        if(!BetterNationsCommand.instance.subCommands.containsKey(context.getArg(1))) return StaticMessages.UNKNOWN_COMMAND;
        context.getSender().sendMessage(new StringMessage("&cNOTE THAT THIS COMMAND ISN'T SAFE AT ALL"));
        context.getSender().sendMessage(new StringMessage("&cONLY FOR TESTING!!! USE IT AT YOUR OWN RISK!!!"));
        return BetterNationsCommand.instance.subCommands.get(context.getArg(1)).execute(ctx);
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg()==1) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        if(context.getCurrentArg()==2) return new ArrayList<>(BetterNationsCommand.instance.subCommands.keySet());
        if(context.checkArgs(ParameterType.PLAYER, ParameterType.STRING)&&BetterNationsCommand.instance.subCommands.containsKey(context.getArg(1))) {
            SimplePlayer target=context.getPlayerArg(0);
            ICommandContext ctx = new CommandContext(context.args().subList(2, context.args().size()), context.getSender(), target, "execute");
            return BetterNationsCommand.instance.subCommands.get(context.getArg(1)).tabComplete(ctx);
        }
        return null;
    }
}
