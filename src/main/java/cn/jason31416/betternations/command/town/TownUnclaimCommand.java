package cn.jason31416.betternations.command.town;

import cn.jason31416.betternations.army.states.SiegeFlag;
import cn.jason31416.betternations.army.states.StructuredArmy;
import cn.jason31416.betternations.manager.EventListener;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TownUnclaimCommand extends ChildCommand {
    public static Message unclaimWithChecks(SimplePlayer player, SimpleChunkLocation chunkLocation){
        Town town = chunkLocation.getTown();
        if(town==null) return Message.getMessage("command.failed.chunk-not-belong-to-town");
        if(!town.getRole(player).hasPermission(Permission.TOWN_UNCLAIM)) return Message.getMessage("command.failed.no-permission");
        if(!town.unclaimChecks(chunkLocation)) return Message.getMessage("command.failed.cannot-unclaim-connecting-chunks");
        if(town.getCore().location.getChunkLocation().equals(chunkLocation)) return Message.getMessage("command.failed.cannot-unclaim-core-chunk");
        for(SimpleChunkLocation adj: chunkLocation.getAdjacentChunks()) {
            if (StructuredArmy.armyLocationMap.containsKey(adj)) {
                for (StructuredArmy i: StructuredArmy.armyLocationMap.get(adj)){
                    if(i instanceof SiegeFlag flag && flag.target == town){
                        return Message.getMessage("command.failed.cannot-unclaim-siege-chunk");
                    }
                }
            }
        }
        if(town.unclaim(chunkLocation)){
            player.addBalance(Config.getDouble("unclaim-refund"));
            if(player.isOnline()&&player.getLocation().getChunkLocation().equals(chunkLocation))
                EventListener.sendCrossChunkMessage(player, player.getLocation().getChunkLocation(), player.getLocation().getChunkLocation());
            return Message.getMessage("command.success.town-chunk-unclaimed");
        }
        else return Message.getMessage("command.failed.chunk-unclaim-failed");
    }
    public TownUnclaimCommand(IParentCommand parent) {
        super("unclaim", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        if(context.getArg(0).equals("auto")){
            if(EventListener.autoClaiming.get(context.getPlayer())!= EventListener.AutoClaimingMode.TOWN_UNCLAIM) {
                Message.getMessage("auto-claiming.town-unclaim").sendActionbar(context.getPlayer());
                EventListener.autoClaiming.put(context.getPlayer(), EventListener.AutoClaimingMode.TOWN_UNCLAIM);
            }else {
                Message.getMessage("auto-claiming.disabled-auto-unclaiming").sendActionbar(context.getPlayer());
                EventListener.autoClaiming.remove(context.getPlayer());
            }
        }
        return unclaimWithChecks(context.getPlayer(), context.getSender().toPlayer().getLocation().getChunkLocation());
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return List.of("auto");
    }
}
