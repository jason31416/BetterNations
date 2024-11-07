package cn.jason31416.betternations.command.town;

import cn.jason31416.betternations.command.nation.NationClaimCommand;
import cn.jason31416.betternations.manager.EventListener;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TownCreateCommand extends ChildCommand {
    public TownCreateCommand(IParentCommand parent) {
        super("create", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if (context.getPlayer() == null||!context.checkArgs(ParameterType.STRING)||!context.getSender().isPlayer()) return null;
        if (context.getPlayer().getNation() == null) return Message.getMessage("command.failed.player-not-in-nation");
        SimpleLocation location = context.getSender().toPlayer().getLocation().getBlockLocation();
        if (!context.getPlayer().getRank().hasPermission(Permission.TOWN_CREATE)) return Message.getMessage("command.failed.no-permission");
        if (location.getChunkLocation().isTownChunk()) return Message.getMessage("command.failed.already-claimed-by-town");
        if (location.getChunkLocation().getNation()!=null&&
                location.getChunkLocation().getNation()!=context.getPlayer().getNation())
            return Message.getMessage("command.failed.chunk-not-belong-to-nation");
        if (location.getBlockMaterial().isSolid()||!location.getRelative(0, -1, 0).getBlockMaterial().isSolid()){
            return Message.getMessage("command.failed.invalid-creation-location");
        }
        if(Town.getTown(context.getArg(0))!=null) return Message.getMessage("command.failed.town-name-exists");
        if(!context.getPlayer().withdrawBalance(Config.getDouble("town.creation-cost"))){
            return Message.getMessage("not-enough-money").add("amount", Config.getDouble("town.creation-cost"));
        }
        Town.createTown(context.getArg(0), location, context.getPlayer().getNation(), context.getPlayer());
        return Message.getMessage("command.success.town-created").add("name", context.getArg(0));
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg()==1) return List.of("<name>");
        return null;
    }
}
