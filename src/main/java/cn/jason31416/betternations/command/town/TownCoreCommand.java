package cn.jason31416.betternations.command.town;

import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.nation.TownRole;
import cn.jason31416.betternations.structure.types.TownCore;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TownCoreCommand extends ChildCommand {
    public TownCoreCommand(IParentCommand parent) {
        super(List.of("core", "nexus"), parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.getSender().isPlayer()) return null;
        Town town = context.getSender().toPlayer().getLocation().getChunkLocation().getTown();
        if(town == null) return Message.getMessage("command.failed.not-in-town");
        if(town.getRole(context.getPlayer())==TownRole.NONE||town.getRole(context.getPlayer())==TownRole.RESIDENT) return Message.getMessage("command.failed.no-permission");
        if(town.core != null){
            town.core.breakStructure();
            town.core.unregister();
        }
        town.core = new TownCore();
        town.core.town = town;
        town.core.location = context.getSender().toPlayer().getLocation().getBlockLocation();
        town.core.place();
        return Message.getMessage("command.success.moved-core");
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
