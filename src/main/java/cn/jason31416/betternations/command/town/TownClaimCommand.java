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
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TownClaimCommand extends ChildCommand {
    public static Message claimWithChecks(SimplePlayer player, SimpleChunkLocation chunkLocation, Town town){
        if(chunkLocation.isTownChunk()){
            return Message.getMessage("command.failed.chunk-already-claimed");
        }
        if(!chunkLocation.isClaimed()){
            Message res = NationClaimCommand.claimWithChecks(player, chunkLocation);
            if(!res.equals(Message.getMessage("command.success.chunk-claimed"))){
                return res;
            }
        }
        if(!player.getRank().hasPermission(Permission.TOWN_CLAIM)&&!town.getRole(player).hasPermission(Permission.TOWN_CLAIM)){
            return Message.getMessage("command.failed.no-permission");
        }
        if(chunkLocation.getNation()!=town.getNation()){
            return Message.getMessage("command.failed.chunk-not-belong-to-nation");
        }
        if(player.getBalance()< Config.getDouble("town.claim-cost")){
            return Message.getMessage("command.failed.not-enough-money").add("amount", Config.getDouble("town.claim-cost"));
        }
        player.withdrawBalance(Config.getDouble("town.claim-cost"));
        if(town.claim(chunkLocation)){
            if(player.getLocation().getChunkLocation().equals(chunkLocation))
                EventListener.sendCrossChunkMessage(player, player.getLocation().getChunkLocation(), player.getLocation().getChunkLocation());
            return Message.getMessage("command.success.town-chunk-claimed");
        }
        else return Message.getMessage("command.failed.chunk-claim-failed");
    }
    public TownClaimCommand(IParentCommand parent) {
        super("claim", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        Town town=null;
        SimpleChunkLocation chunkLocation = context.getSender().toPlayer().getLocation().getChunkLocation();
        if(!context.args().isEmpty()){
            if(context.getArg(0).equals("auto")){
                if(!chunkLocation.isTownChunk()) return Message.getMessage("auto-claiming.cannot-start-from-wild");
                if(EventListener.autoClaiming.get(context.getPlayer())!= EventListener.AutoClaimingMode.TOWN_CLAIM) {
                    Message.getMessage("auto-claiming.town-claim").sendActionbar(context.getPlayer());
                    EventListener.autoClaiming.put(context.getPlayer(), EventListener.AutoClaimingMode.TOWN_CLAIM);
                }else {
                    Message.getMessage("auto-claiming.disabled-auto-claiming").sendActionbar(context.getPlayer());
                    EventListener.autoClaiming.remove(context.getPlayer());
                }
                return null;
            }
            town = Town.getTown(context.getArg(0));
            if(town==null) return Message.getMessage("command.failed.town-not-exist");
            boolean bb = false;
            for(SimpleChunkLocation adjs: chunkLocation.getAdjacentChunks()){
                if(adjs.getTown()==town){
                    bb = true;
                    break;
                }
            }
            if(!bb){
                return Message.getMessage("command.failed.chunk-not-adjacent-to-town");
            }
        }else{
            for(SimpleChunkLocation adjs: chunkLocation.getAdjacentChunks()){
                if(adjs.getTown()!=null&&adjs.getTown().getNation() == context.getPlayer().getNation()){
                    town = adjs.getTown();
                    break;
                }
            }
            if(town==null) return Message.getMessage("command.failed.chunk-not-adjacent-to-any-town");
        }
        return claimWithChecks(context.getPlayer(), chunkLocation, town);
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return List.of("auto");
    }
}
