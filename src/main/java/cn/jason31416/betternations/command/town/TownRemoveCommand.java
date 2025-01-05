package cn.jason31416.betternations.command.town;

import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.nation.TownRole;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class TownRemoveCommand extends ChildCommand {
    public TownRemoveCommand(IParentCommand parent) {
        super("remove", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.getSender().isPlayer()) return null;
        Town town = context.getSender().toPlayer().getLocation().getChunkLocation().getTown();
        if(town == null) return Message.getMessage("command.failed.not-in-town");
        if(!context.getPlayer().equals(town.mayor)||town.getNation().getTowns().size()<=1) return Message.getMessage("command.failed.no-permission");
        town.remove();
        return Message.getMessage("command.success.removed-town").add("town", town.getName());
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return List.of("PROCEED WITH CAUTION!!!");
    }
}
