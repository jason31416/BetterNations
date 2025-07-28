package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.army.states.InvasionFlag;
import cn.jason31416.betternations.army.states.SiegeFlag;
import cn.jason31416.betternations.army.states.StructuredArmy;
import cn.jason31416.betternations.command.chat.ChatCommand;
import cn.jason31416.betternations.command.nation.NationClaimCommand;
import cn.jason31416.betternations.command.nation.NationUnclaimCommand;
import cn.jason31416.betternations.command.town.TownClaimCommand;
import cn.jason31416.betternations.command.town.TownUnclaimCommand;
import cn.jason31416.betternations.nation.*;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

import java.util.*;

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
    @EventHandler(
            priority = EventPriority.LOW,
            ignoreCancelled = true
    )
    public void onChat(PlayerChatEvent event){
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        ChatCommand.ChatInfo ci = (ChatCommand.playerChatMap.getOrDefault(player.getName(), ChatCommand.CTGlobal));
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
        a: if (ci.type == ChatCommand.ChatType.NATION) {
            if (player.getNation() == null) break a;
            Message message1 = message.add("domain", Message.getMessage("chat.prefix_nation"));
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (Nation.getNation(p.getName()) == player.getNation()) {
                    message1.send(p);
                }
            }
            message.add("domain", player.getNation()).send(Bukkit.getConsoleSender());
            return;
        } else
        b: if (ci.type == ChatCommand.ChatType.TOWN) {
            Town t = ci.town;
            if (t == null || t.getRole(player) == TownRole.NONE) {
                ChatCommand.playerChatMap.put(player.getName(), ChatCommand.CTGlobal);
                break b;
            }
            message = message.add("domain", Message.getMessage("chat.prefix_town").add("town", ci.town));
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!t.getRole(SimplePlayer.of(p)).equals(TownRole.NONE)) {
                    message.send(p);
                }
            }
            message.send(Bukkit.getConsoleSender());
            return;
        }
        message.add("domain", Message.getMessage("chat.prefix_global")).broadcast();
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
    public void onTeleport(PlayerTeleportEvent event){
        if(event.getTo() == null) return;
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        SimpleChunkLocation from = SimpleLocation.of(event.getFrom()).getChunkLocation();
        SimpleChunkLocation to = SimpleLocation.of(event.getTo()).getChunkLocation();
        if(from.isTownChunk()!=to.isTownChunk()||from.getTown()!=to.getTown()||from.getNation()!=to.getNation()||from.isClaimed()!=to.isClaimed()) {
            sendCrossChunkMessage(player, from, to);
        }
    }
    @EventHandler
    public void onMove(PlayerMoveEvent event){
        if(event.getTo() == null) return;
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        SimpleChunkLocation from = SimpleLocation.of(event.getFrom()).getChunkLocation();
        SimpleChunkLocation to = SimpleLocation.of(event.getTo()).getChunkLocation();
        if(to.getNation() != null) {
            boolean bb = to.getNation().getRelation(player.getNation()) != Relation.ALLY;
            if (!bb) for (StructuredArmy i : StructuredArmy.armyLocationMap.getOrDefault(to, new HashSet<>())) {
                if (i.stack.nation.getRelation(to.getNation()) != Relation.ALLY){
                    bb = true;
                    break;
                }
            }
            if (Config.getBoolean("nation.prevent-unfriendly-elytra", false) && bb) {
                if (event.getPlayer().isGliding()) {
                    event.getPlayer().setGliding(false);
                    Message.getMessage("town.cannot-fly").sendActionbar(player);
                }
            }
        }
        if(!from.equals(to)){
            if(autoClaiming.containsKey(SimplePlayer.of(event.getPlayer()))){
                switch (autoClaiming.get(SimplePlayer.of(event.getPlayer()))){
                    case CLAIM -> NationClaimCommand.claimWithChecks(player, to).send(event.getPlayer());
                    case UNCLAIM -> NationUnclaimCommand.unclaimWithChecks(player, to).send(event.getPlayer());
                    case TOWN_CLAIM -> {if(from.getTown()!=null) TownClaimCommand.claimWithChecks(player, to, from.getTown()).send(event.getPlayer());}
                    case TOWN_UNCLAIM -> TownUnclaimCommand.unclaimWithChecks(player, to).send(event.getPlayer());
                }
            }else if(from.isTownChunk()!=to.isTownChunk()||from.getTown()!=to.getTown()||from.getNation()!=to.getNation()||from.isClaimed()!=to.isClaimed()) {
                sendCrossChunkMessage(player, from, to);
            }
        }
    }
    @EventHandler
    public void onWaterFlow(BlockFromToEvent event){
        SimpleChunkLocation to = SimpleLocation.of(event.getToBlock()).getChunkLocation();
        SimpleChunkLocation from = SimpleLocation.of(event.getBlock()).getChunkLocation();
        if(to.getNation()!=from.getNation()) event.setCancelled(true);
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        if(!player.hasPermission(Permission.BUILD, SimpleLocation.of(event.getBlock()))){
            event.setCancelled(true);
            Message.getMessage("town.cannot-build").sendActionbar(player);
        }
    }
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        if(!player.hasPermission(Permission.BUILD, SimpleLocation.of(event.getBlock()))){
            event.setCancelled(true);
            Message.getMessage("town.cannot-build").sendActionbar(player);
        }else {
            SimpleChunkLocation chunk = SimpleLocation.of(event.getBlock()).getChunkLocation();
            if (chunk.isTownChunk()) {
                Town t = chunk.getTown();
                Objects.requireNonNull(t);
                t.devPoints += Config.getDouble("town.dev-points.block-place");
            }
        }
    }
    @EventHandler
    public void onPlayerDamageVehicle(VehicleDestroyEvent event){
        SimpleLocation loc = SimpleLocation.of(event.getVehicle().getLocation());
        if(event.getAttacker() instanceof Player dmger) {
            if (loc.getChunkLocation().isTownChunk() && loc.getChunkLocation().getNation() != SimplePlayer.of(dmger).getNation()){
                event.setCancelled(true);
            }
        }else if(event.getAttacker() instanceof Projectile pj){
            if(pj.getShooter() instanceof Player pl){
                if (loc.getChunkLocation().isTownChunk() && loc.getChunkLocation().getNation() != SimplePlayer.of(pl).getNation()){
                    event.setCancelled(true);
                }
            }
        }
    }
    @EventHandler
    public void onPlayerAttackedInTown(EntityDamageByEntityEvent event){
//        BetterNations.instance.getLogger().info("onPlayerAttackedInTown: "+event.getEntity().getClass().getName());
        if(event.getEntity() instanceof Player pl){
            SimpleLocation loc = SimpleLocation.of(pl.getLocation());
            SimplePlayer sp = SimplePlayer.of(pl);
            if(loc.getChunkLocation().isTownChunk()&&loc.getChunkLocation().getNation()==sp.getNation()){
                if(event.getDamager() instanceof Player dmger){
                    SimplePlayer sdmger = SimplePlayer.of(dmger);
                    if(sdmger.getNation()!=sp.getNation()){
                        event.setCancelled(true);
                    }
                }else if(event.getDamager() instanceof Projectile pj){
                    if(pj.getShooter() instanceof Player dmger){
                        SimplePlayer sdmger = SimplePlayer.of(dmger);
                        if(sdmger.getNation()!=sp.getNation()){
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }else if(!(event.getEntity() instanceof Monster)){
            SimpleLocation loc = SimpleLocation.of(event.getEntity().getLocation());
            if(event.getDamager() instanceof Player dmger) {
                if (loc.getChunkLocation().isTownChunk() && loc.getChunkLocation().getNation() != SimplePlayer.of(dmger).getNation()){
                    event.setCancelled(true);
                }
            }else if(event.getDamager() instanceof Projectile pj){
                if(pj.getShooter() instanceof Player pl){
                    if (loc.getChunkLocation().isTownChunk() && loc.getChunkLocation().getNation() != SimplePlayer.of(pl).getNation()){
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event){
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        if(event.getClickedBlock()==null) return;
        if(AbstractStructure.structures.containsKey(SimpleLocation.of(event.getClickedBlock()))&&
                AbstractStructure.structures.get(SimpleLocation.of(event.getClickedBlock())) instanceof StructuredArmy) return;
        if(!player.hasPermission(Permission.BUILD, SimpleLocation.of(event.getClickedBlock()))){
            event.setCancelled(true);
            Message.getMessage("town.cannot-build").sendActionbar(player);
        }
    }
    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event){
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        if(!player.hasPermission(Permission.BUILD, SimpleLocation.of(event.getRightClicked().getLocation()))){
            event.setCancelled(true);
            Message.getMessage("town.cannot-build").sendActionbar(player);
        }
    }
    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event){
        for(Block i: event.getBlocks()){
            SimpleLocation tolocation = SimpleLocation.of(i);
            SimpleLocation fromLocation = SimpleLocation.of(i.getRelative(event.getDirection(), 1).getLocation());
            if(tolocation.getChunkLocation().getNation()!=fromLocation.getChunkLocation().getNation()){
                event.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler
    public void onPistonPush(BlockPistonExtendEvent event){
        for(Block i: event.getBlocks()){
            SimpleLocation tolocation = SimpleLocation.of(i);
            SimpleLocation fromLocation = SimpleLocation.of(i.getRelative(event.getDirection(), 1).getLocation());
            if(tolocation.getChunkLocation().getNation()!=fromLocation.getChunkLocation().getNation()){
                event.setCancelled(true);
                return;
            }
        }
    }
    private void handleExplosion(List<Block> blocks){
        Map<SimpleChunkLocation, Boolean> isInvasionCache = new HashMap<>();
        blocks.removeIf(block -> {
            SimpleLocation loc = SimpleLocation.of(block);
            if(StructuredArmy.armyLocationMap.containsKey(loc.getChunkLocation())){
                if(!isInvasionCache.containsKey(loc.getChunkLocation())) {
                    for (StructuredArmy i : StructuredArmy.armyLocationMap.get(loc.getChunkLocation())) {
                        if (i instanceof InvasionFlag || i instanceof SiegeFlag) {
                            isInvasionCache.put(loc.getChunkLocation(), true);
                            return true;
                        }
                    }
                    isInvasionCache.put(loc.getChunkLocation(), false);
                }else if(isInvasionCache.get(loc.getChunkLocation())){
                    return true;
                }
            }
            return loc.getChunkLocation().isTownChunk()||AbstractStructure.structures.containsKey(loc);
        });
    }
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event){
        handleExplosion(event.blockList());
    }
    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event){
        handleExplosion(event.blockList());
    }
}
