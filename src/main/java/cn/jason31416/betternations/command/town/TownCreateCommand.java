package cn.jason31416.betternations.command.town;

import cn.jason31416.betternations.army.states.StructuredArmy;
import cn.jason31416.betternations.command.nation.NationClaimCommand;
import cn.jason31416.betternations.manager.HistoricalBroadcastManager;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
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
        if (Config.getBoolean("town.require-ruin")) return Message.getMessage("command.failed.require-ruin");
        if (context.getPlayer().getNation() == null) return Message.getMessage("command.failed.player-not-in-nation");
        SimpleLocation location = context.getSender().toPlayer().getLocation().getBlockLocation();
        if(!NationClaimCommand.checkWorld(location.world())){
            return Message.getMessage("command.failed.chunk-claim-invalid-world");
        }
        if (!context.getPlayer().getRank().hasPermission(Permission.TOWN_CREATE)) return Message.getMessage("command.failed.no-permission");
        if (location.getChunkLocation().isTownChunk()) return Message.getMessage("command.failed.already-claimed-by-town");
        if (location.getChunkLocation().getNation()!=null&&
                location.getChunkLocation().getNation()!=context.getPlayer().getNation())
            return Message.getMessage("command.failed.chunk-not-belong-to-nation");
        if (location.getBlockMaterial().isSolid()||!location.getRelative(0, -1, 0).getBlockMaterial().isSolid()||StructuredArmy.isInvasionChunk(location.getChunkLocation())){
            return Message.getMessage("command.failed.invalid-creation-location");
        }
        if(Town.getTown(context.getArg(0))!=null) return Message.getMessage("command.failed.town-name-exists");
        if(!context.getPlayer().withdrawBalance(Config.getDouble("town.creation-cost"))){
            return Message.getMessage("not-enough-money").add("amount", Config.getDouble("town.creation-cost"));
        }
        Town town = Town.createTown(context.getArg(0), location, context.getPlayer().getNation(), context.getPlayer());
        if(town != null) HistoricalBroadcastManager.broadcast(Message.getMessage("history.town-creation")
                .add("player", context.getPlayer().getName()).add("nation", town.getNation().getName()).add("town", town.getName()), List.of(town.getNation()));
        return Message.getMessage("command.success.town-created").add("name", context.getArg(0));
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg()==1) return List.of("<name>");
        return null;
    }
}
