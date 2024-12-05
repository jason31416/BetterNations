package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.command.town.TownUnclaimCommand;
import cn.jason31416.betternations.manager.EventListener;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class NationUnclaimCommand extends ChildCommand {
    public static Message unclaimWithChecks(SimplePlayer player, SimpleChunkLocation chunkLocation){
        Nation nation = player.getNation();
        if(nation==null){
            return Message.getMessage("command.failed.player-not-in-nation");
        }
        if(chunkLocation.isTownChunk()){
            Message res = TownUnclaimCommand.unclaimWithChecks(player, chunkLocation);
            if(!res.equals(Message.getMessage("command.success.town-chunk-unclaimed"))){
                return res;
            }
        }
        if(!chunkLocation.isClaimed()){
            return Message.getMessage("command.failed.chunk-not-claimed");
        }
        if(chunkLocation.getNation()!=nation){
            return Message.getMessage("command.failed.chunk-not-belong-to-nation");
        }
        if(!player.getRank().hasPermission(Permission.NATION_UNCLAIM)){
            return Message.getMessage("command.failed.no-permission");
        }
        if(chunkLocation.isTownChunk()){
            if(!player.getRank().hasPermission(Permission.TOWN_UNCLAIM)&&
                    !Objects.requireNonNull(chunkLocation.getTown()).getRole(player).hasPermission(Permission.TOWN_UNCLAIM)){
                return Message.getMessage("command.failed.player-no-permission");
            }
            if(!Objects.requireNonNull(chunkLocation.getTown()).unclaim(chunkLocation)){
                return Message.getMessage("command.failed.chunk-unclaim-failed");
            }
        }
        if(nation.unclaim(chunkLocation)){
            if(player.isOnline()&&player.getLocation().getChunkLocation().equals(chunkLocation))
                EventListener.sendCrossChunkMessage(player, player.getLocation().getChunkLocation(), player.getLocation().getChunkLocation());
            player.addBalance(Config.getDouble("nation.unclaim-refund"));
            return Message.getMessage("command.success.chunk-unclaimed");
        }
        else return Message.getMessage("command.failed.chunk-unclaim-failed");
    }
    public NationUnclaimCommand(IParentCommand parent) {
        super("unclaim", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        if(context.getArg(0).equals("auto")){
            if(EventListener.autoClaiming.get(context.getPlayer())!= EventListener.AutoClaimingMode.UNCLAIM) {
                Message.getMessage("auto-claiming.unclaim").sendActionbar(context.getPlayer());
                EventListener.autoClaiming.put(context.getPlayer(), EventListener.AutoClaimingMode.UNCLAIM);
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
