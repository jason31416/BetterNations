package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.manager.HistoricalBroadcastManager;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.NationType;
import cn.jason31416.betternations.structure.types.TownRuin;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static cn.jason31416.planetlib.command.ParameterType.STRING;

public class NationCreateCommand extends ChildCommand {
    public NationCreateCommand(IParentCommand parent) {
        super("create", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.getSender().isPlayer()) return null;
        if(context.getPlayer().getNation()!=null){
            return Message.getMessage("command.failed.player-already-in-nation");
        }
        if(context.getSender().toPlayer().getLocation().getChunkLocation().isClaimed()){
            return Message.getMessage("command.failed.already-claimed-by-nation");
        }
        if(!NationClaimCommand.checkWorld(context.getPlayer().getLocation().world())){
            return Message.getMessage("command.failed.chunk-claim-invalid-world");
        }
        String nationName = context.getArg(0);
        if(Nation.getNation(nationName)!=null){
            return Message.getMessage("command.failed.nation-already-exists");
        }
        if(nationName.length()<Config.getInt("naming.min-length")||nationName.length()>Config.getInt("naming.max-length")){
            return Message.getMessage("command.failed.name-length-invalid");
        }
        double amount = Config.getDouble("nation.creation-cost");
        if(context.getPlayer().getBalance()<amount){
            return Message.getMessage("command.failed.not-enough-money").add("amount", amount);
        }
        SimpleLocation loc = context.getSender().toPlayer().getLocation();
        String townName = null;
        if(Config.getBoolean("town.require-ruin")){
            TownRuin ruin = TownRuin.ruins.getOrDefault(loc.getChunkLocation(), null);
            if(ruin==null){
                if(Config.getBoolean("town.allow-nomadic")){
                    loc = null;
                }else return Message.getMessage("command.failed.no-ruin-in-chunk");
            }else {
                loc = TownRuin.ruins.get(loc.getChunkLocation()).location;
                townName = ruin.name;
                ruin.breakStructure();
                ruin.unregister();
            }
        }
        Nation nation = null;
        if(loc != null) nation = Nation.createNation(context.getPlayer(), loc, nationName, townName);
        else nation = Nation.createNation(context.getPlayer(), nationName);
        if(nation==null){
            return Message.getMessage("command.failed.failed-create-nation").add("name", nationName);
        }
        context.getPlayer().withdrawBalance(amount);
        context.getSender().sendMessage(Message.getMessage("command.success.nation-created").add("name", nationName));
        HistoricalBroadcastManager.broadcast(Message.getMessage("history.nation-creation")
                .add("player", context.getPlayer().getName())
                .add("nation", nation.getName()),
                List.of(nation));
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return List.of("<name>");
    }
}
