package cn.jason31416.betternations.command.ruin;

import cn.jason31416.betternations.structure.types.TownRuin;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TownRuinCommand extends ChildCommand {

    public TownRuinCommand(IParentCommand parent) {
        super("ruin", parent);
    }

    @Override
    public @Nullable Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.checkArgs(ParameterType.STRING)) return null;
        String name = context.getArg(0);
        if(!context.getPlayer().getPlayer().isOp()) return Message.getMessage("command.failed.no-permission");
        if(TownRuin.ruins.containsKey(context.getPlayer().getLocation().getBlockLocation().getChunkLocation())||context.getPlayer().getLocation().getBlockLocation().getChunkLocation().isTownChunk()) return Message.getMessage("command.failed.ruin-exists");
        TownRuin.create(context.getPlayer().getLocation().getBlockLocation(), name);
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return List.of("<Name>");
    }
}
