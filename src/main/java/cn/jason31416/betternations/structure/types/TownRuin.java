package cn.jason31416.betternations.structure.types;

import cn.jason31416.betternations.command.nation.NationClaimCommand;
import cn.jason31416.betternations.manager.HistoricalBroadcastManager;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.nation.TownRole;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.tempAction.TempAction;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class TownRuin extends AbstractStructure {
    public static Map<SimpleChunkLocation, TownRuin> ruins = new HashMap<>();
    public String name;
    public TownRuin() {
    }
    public String getHologramText(){
        return Message.getMessage("structure.townruin.hologram").add("town", name).toString();
    }
    @Override
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public boolean processInteraction(InteractionType type, SimplePlayer player){
        if(type == InteractionType.INTERACT){
            player.sendMessage(Message.getMessage("structure.townruin.message.interact").add("cost", Config.getDouble("town.creation-cost")));
        }else if(type == InteractionType.BREAK) {
            breakStructure();
            unregister();
            return false;
        }else if(type == InteractionType.SNEAK_CLICK){
            if(location.getChunkLocation().isClaimed()&&player.getNation()!=location.getChunkLocation().getNation()){
                player.sendMessage(Message.getMessage("structure.townruin.message.claimed"));
                return true;
            }
            if(player.getNation() == null){
                player.sendMessage(Message.getMessage("structure.townruin.message.no_nation"));
                return true;
            }
            if(!NationClaimCommand.checkWorld(location.world())){
                player.sendMessage(Message.getMessage("command.failed.chunk-claim-invalid-world"));
                return true;
            }
            if(Town.getTown(name)!=null){
                player.sendMessage(Message.getMessage("command.failed.town-name-exists"));
                return true;
            }
            if(!player.withdrawBalance(Config.getDouble("town.creation-cost"))){
                player.sendMessage(Message.getMessage("not-enough-money").add("amount", Config.getDouble("town.creation-cost")));
                return true;
            }
            breakStructure();
            unregister();
            player.sendMessage(Message.getMessage("command.success.town-created").add("name", name));
            Town town = Town.createTown(name, location, player.getNation(), player);
            if(town != null) HistoricalBroadcastManager.broadcast(Message.getMessage("history.town-creation")
                    .add("player", player.getName()).add("nation", town.getNation().getName()).add("town", name), List.of(town.getNation()));
            return true;
        }
        return true;
    }

    public void register(){
        super.register();
        ruins.put(location.getChunkLocation(), this);
    }
    public void unregister(){
        super.unregister();
        ruins.remove(location.getChunkLocation());
    }

    public static void create(SimpleLocation location, String name){
        TownRuin townRuin = new TownRuin();
        townRuin.location = location;
        townRuin.name = name;
        townRuin.place();
    }

    @Override
    public Material getMaterial() {
        return Material.GLASS;
    }

    @Override
    public boolean serialize(IDataItem dataItem) {
        dataItem.set("town", name);
        return true;
    }

    @Override
    public void deserialize(IDataItem dataItem) {
        name = dataItem.getString("town");
    }
}
