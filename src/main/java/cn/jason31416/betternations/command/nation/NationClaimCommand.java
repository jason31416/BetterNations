package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.manager.EventListener;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.jason31416.planetlib.command.ParameterType.*;

public class NationClaimCommand extends ChildCommand {
    public static Message claimWithChecks(SimplePlayer player, SimpleChunkLocation chunkLocation){
        Nation nation = player.getNation();
        if(nation==null){
            return Message.getMessage("command.failed.player-not-in-nation");
        }
        if(chunkLocation.isClaimed()){
            return Message.getMessage("command.failed.chunk-already-claimed");
        }
        if(!player.getRank().hasPermission(Permission.NATION_CLAIM)){
            return Message.getMessage("command.failed.no-permission");
        }
        if(player.getBalance()< Config.getDouble("nation.claim-cost")){
            return Message.getMessage("command.failed.not-enough-money").add("amount", Config.getDouble("nation.claim-cost"));
        }
        player.withdrawBalance(Config.getDouble("nation.claim-cost"));
        if(nation.claim(chunkLocation)){
            if(player.getLocation().getChunkLocation().equals(chunkLocation))
                EventListener.sendCrossChunkMessage(player, player.getLocation().getChunkLocation(), player.getLocation().getChunkLocation());
            return Message.getMessage("command.success.chunk-claimed");
        }
        else return Message.getMessage("command.failed.chunk-claim-failed");
    }
    public NationClaimCommand(IParentCommand parent) {
        super("claim", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        if(context.args().isEmpty()){
            return claimWithChecks(context.getPlayer(), context.getSender().toPlayer().getLocation().getChunkLocation());
        }
        if(context.getArg(0).equals("auto")) {
            if(EventListener.autoClaiming.get(context.getPlayer())!= EventListener.AutoClaimingMode.CLAIM) {
                Message.getMessage("auto-claiming.claim").sendActionbar(context.getPlayer());
                EventListener.autoClaiming.put(context.getPlayer(), EventListener.AutoClaimingMode.CLAIM);
            }else {
                Message.getMessage("auto-claiming.disabled-auto-claiming").sendActionbar(context.getPlayer());
                EventListener.autoClaiming.remove(context.getPlayer());
            }
            return claimWithChecks(context.getPlayer(), context.getSender().toPlayer().getLocation().getChunkLocation());
        }else if(context.getArg(0).equals("square")){
            if(context.checkArgs(STRING, INTEGER)){
                int size = Integer.parseInt(context.getArg(1));
                Set<SimpleChunkLocation> chunks = new HashSet<>();
                SimpleChunkLocation center = context.getSender().toPlayer().getLocation().getChunkLocation();
                int count=0;
                double playerBalance=context.getPlayer().getBalance();
                for(int i=-size;i<=size;i++) for(int j=-size;j<=size;j++){
                    SimpleChunkLocation chunk = center.getRelative(i, j);
                    if(chunk.isClaimed()) continue;
                    count++;
                    if(count*Config.getDouble("nation.claim-cost")>playerBalance){
                        return Message.getMessage("command.failed.not-enough-money").add("amount", count*Config.getDouble("nation.claim-cost"));
                    }
                    chunks.add(chunk);
                }
                if(count==0) return Message.getMessage("command.failed.no-available-chunk");
                context.getPlayer().withdrawBalance(count*Config.getDouble("nation.claim-cost"));
                for(SimpleChunkLocation chunk : chunks){
                    Message message = claimWithChecks(context.getPlayer(), chunk);
                    if(!message.equals(Message.getMessage("command.success.chunk-claimed"))){
                        message.send(context.getSender());
                    }
                }
                return Message.getMessage("command.success.claimed-chunks").add("count", count);
            }
        }
        return Message.getMessage("command.failed.unknown-subcommand");
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg()==1) return List.of("auto", "square");
        else if(context.getCurrentArg()==2&&context.getArg(0).equals("square")) return List.of("<radius>");
        return null;
    }
}
