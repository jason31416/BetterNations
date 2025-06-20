package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.manager.EventListener;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleWorld;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static cn.jason31416.planetlib.command.ParameterType.INTEGER;
import static cn.jason31416.planetlib.command.ParameterType.STRING;

public class NationClaimCommand extends ChildCommand {
    public static boolean checkWorld(SimpleWorld world){
        return Config.getConfig().getStringList("enabled-worlds").isEmpty() || Config.getConfig().getStringList("enabled-worlds").contains(world.getName());
    }
    public static Message claimWithChecks(SimplePlayer player, SimpleChunkLocation chunkLocation, boolean doCost){
        Nation nation = player.getNation();
        if(nation==null){
            return Message.getMessage("command.failed.player-not-in-nation");
        }
        if(!checkWorld(chunkLocation.world())){
            return Message.getMessage("command.failed.chunk-claim-invalid-world");
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
        if(doCost) player.withdrawBalance(Config.getDouble("nation.claim-cost"));
        if(nation.claim(chunkLocation)){
            if(player.isOnline()&&player.getLocation().getChunkLocation().equals(chunkLocation))
                EventListener.sendCrossChunkMessage(player, player.getLocation().getChunkLocation(), player.getLocation().getChunkLocation());
            return Message.getMessage("command.success.chunk-claimed");
        }
        else return Message.getMessage("command.failed.chunk-claim-failed");
    }
    public static Message claimWithChecks(SimplePlayer player, SimpleChunkLocation chunkLocation){
        return claimWithChecks(player, chunkLocation, true);
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
                new BukkitRunnable(){
                    @Override
                    public void run() {
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
                                Message.getMessage("command.failed.not-enough-money").add("amount", count*Config.getDouble("nation.claim-cost")).send(context.getSender());
                                return;
                            }
                            chunks.add(chunk);
                        }
                        if(count==0){
                            Message.getMessage("command.failed.no-available-chunk").send(context.getSender());
                            return;
                        }
                        context.getPlayer().withdrawBalance(count*Config.getDouble("nation.claim-cost"));
                        for(SimpleChunkLocation chunk : chunks){
                            Message message = claimWithChecks(context.getPlayer(), chunk, false);
                            if(!message.equals(Message.getMessage("command.success.chunk-claimed"))){
                                message.send(context.getSender());
                            }
                        }
                        Message.getMessage("command.success.claimed-chunks").add("count", count).send(context.getSender());
                    }
                }.runTaskAsynchronously(BetterNations.instance);
                return null;
            }
        }else if(context.getArg(0).equals("fill")) {
            new BukkitRunnable() {
                @Override
            public void run() {
                Set<SimpleChunkLocation> chunks = new HashSet<>();
                Queue<SimpleChunkLocation> q = new ArrayDeque<>();
                SimpleChunkLocation center = context.getSender().toPlayer().getLocation().getChunkLocation();
                if(center.isClaimed()){
                    Message.getMessage("command.failed.no-available-chunk").send(context.getSender());
                    return;
                }
                int count = 0;
                double playerBalance = context.getPlayer().getBalance();
                q.add(center);
                chunks.add(center);
                while (!q.isEmpty()){
                    SimpleChunkLocation cur = q.poll();
                    count++;
                    if(count * Config.getDouble("nation.claim-cost")>playerBalance){
                        Message.getMessage("command.failed.not-enough-money").add("amount", count*Config.getDouble("nation.claim-cost")).send(context.getSender());
                        return;
                    }
                    for(SimpleChunkLocation loc: cur.getAdjacentChunks()){
                        if(!loc.isClaimed()&&!chunks.contains(loc)){
                            chunks.add(loc);
                            q.add(loc);
                        }
                    }
                    if(!context.getPlayer().isOnline()) return;
                }
                context.getPlayer().withdrawBalance(count * Config.getDouble("nation.claim-cost"));
                for (SimpleChunkLocation chunk : chunks) {
                    Message message = claimWithChecks(context.getPlayer(), chunk, false);
                    if (!message.equals(Message.getMessage("command.success.chunk-claimed"))) {
                        message.send(context.getSender());
                    }
                }
                Message.getMessage("command.success.claimed-chunks").add("count", count).send(context.getSender());
            }
        }.runTaskAsynchronously(BetterNations.instance);
        return null;
        }
        return Message.getMessage("command.failed.unknown-subcommand");
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg()==1) return List.of("auto", "square", "fill");
        else if(context.getCurrentArg()==2&&context.getArg(0).equals("square")) return List.of("<radius>");
        return null;
    }
}
