package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.command.nation.NationClaimCommand;
import cn.jason31416.betternations.command.nation.NationUnclaimCommand;
import cn.jason31416.betternations.command.town.TownClaimCommand;
import cn.jason31416.betternations.command.town.TownUnclaimCommand;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.message.StringMessage;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.event.block.*;

import cn.jason31416.planetlib.wrapper.*;
import cn.jason31416.planetlib.Config;
import org.jetbrains.annotations.Blocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventListener implements Listener {
    public enum AutoClaimingMode {
        CLAIM,
        UNCLAIM,
        TOWN_CLAIM,
        TOWN_UNCLAIM
    }
    public static Map<SimplePlayer, AutoClaimingMode> autoClaiming = new HashMap<>();
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        autoClaiming.remove(SimplePlayer.of(event.getPlayer()));
    }
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onChat(PlayerChatEvent event){
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        Nation nation = player.getNation();
        event.setCancelled(true);
        Message message;
        if(nation != null){
            message = Message.getMessage("chat.with_nation")
                    .add("sender", player.getName())
                    .add("nation", nation.getName())
                    .add("nation_color", nation.getColorTag())
                    .add("title", player.getRank().getDisplayName())
                    .add("message", event.getMessage().replace("<", "\\<"));
        }else{
            message = Message.getMessage("chat.no_nation")
                    .add("sender", player.getName())
                    .add("message", event.getMessage().replace("<", "\\<"));
        }
        message.broadcast();
    }
    public static void sendCrossChunkMessage(SimplePlayer player, SimpleChunkLocation from, SimpleChunkLocation to){
        String title, subtitle;
        if(to.getNation()!=null){
            title = new StringMessage(to.getNation().getColorTag()+to.getNation().getName()).toString();
            if(to.getTown()!=null) {
                subtitle = to.getTown().getName();
            }else{
                subtitle = Message.getMessage("town.wilderness").toString();
            }
        }else{
            title = "";
            subtitle = Message.getMessage("town.wilderness").toString();
        }
        player.sendTitle(title, subtitle, 10, 20, 10);

    }
    @EventHandler
    public void onMove(PlayerMoveEvent event){
        if(event.getTo() == null) return;
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        SimpleChunkLocation from = SimpleLocation.of(event.getFrom()).getChunkLocation();
        SimpleChunkLocation to = SimpleLocation.of(event.getTo()).getChunkLocation();
        if(!from.equals(to)){
            if(autoClaiming.containsKey(SimplePlayer.of(event.getPlayer()))){
                switch (autoClaiming.get(SimplePlayer.of(event.getPlayer()))){
                    case CLAIM -> NationClaimCommand.claimWithChecks(player, to).send(event.getPlayer());
                    case UNCLAIM -> NationUnclaimCommand.unclaimWithChecks(player, to).send(event.getPlayer());
                    case TOWN_CLAIM -> {if(from.getTown()!=null) TownClaimCommand.claimWithChecks(player, to, from.getTown()).send(event.getPlayer());}
                    case TOWN_UNCLAIM -> TownUnclaimCommand.unclaimWithChecks(player, to).send(event.getPlayer());
                }
            }else if(from.isTownChunk()!=to.isTownChunk()||from.getTown()!=to.getTown()||from.isClaimed()!=to.isClaimed()) {
                sendCrossChunkMessage(player, from, to);
            }
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        if(!SimpleLocation.of(event.getBlock()).canInteract(player)){
            event.setCancelled(true);
            Message.getMessage("town.cannot-build").sendActionbar(player);
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        if(!SimpleLocation.of(event.getBlock()).canInteract(player)){
            event.setCancelled(true);
            Message.getMessage("town.cannot-build").sendActionbar(player);
        }
    }
    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event){
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        if(event.getClickedBlock()==null) return;
        if(!SimpleLocation.of(event.getClickedBlock()).canInteract(player)){
            event.setCancelled(true);
            Message.getMessage("town.cannot-build").sendActionbar(player);
        }
    }
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event){
        for(Block i: event.getBlocks()){
            if(SimpleLocation.of(i).getChunkLocation().isTownChunk()){
                event.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler
    public void onBlockExplode(EntityExplodeEvent event){
        event.blockList().removeIf(i -> (SimpleLocation.of(i).getChunkLocation().isTownChunk()||AbstractStructure.structures.containsKey(SimpleLocation.of(i))));
    }
}
