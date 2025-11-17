package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.manager.EventListener;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static cn.jason31416.planetlib.command.ParameterType.INTEGER;
import static cn.jason31416.planetlib.command.ParameterType.STRING;

public class NationInfoCommand extends ChildCommand {
    public NationInfoCommand(IParentCommand parent) {
        super("info", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getArg(0).isEmpty()) return Message.getMessage("command.failed.info-target-not-found");
        Nation nation = Nation.getNation(context.getArg(0));
//        if(nation == null){
//            nation = SimplePlayer.of(context.getArg(0)).getNation();
//        }
        if(nation!=null){
            Nation nation1 = nation;
            MessageLoader.getList("info.nation")
                    .add("nation", nation.getColorTag()+nation.getName())
                    .add("type", nation.getType().getDisplayName())
                    .add("size", nation.nationalChunks.size())
                    .add("leader_role_name", nation.getType().getOwnerRank().getDisplayName())
                    .add("leader", nation.getOwner().getName())
                    .add("members_count", nation.getMembers().size())
                    .add("members", String.join(",", nation.getMembers().stream().map(s->nation1.getRank(s).getDisplayName()+" "+s.getName()).toList()))
                    .add("allies", String.join(",", nation.relations.keySet().stream().filter(n->nation1.getRelation(n)==Relation.ALLY).map(Nation::getName).toList()))
                    .add("enemies", String.join(",", nation.relations.keySet().stream().filter(n->nation1.getRelation(n)==Relation.ENEMY).map(Nation::getName).toList()))
                    .send(context.getSender());
            return null;
        }
        Town town = Town.getTown(context.getArg(0));
        if(town!=null){
            MessageLoader.getList("info.town")
                    .add("nation", town.getNation().getColorTag()+town.getNation().getName())
                    .add("town", ""+town.getName())
                    .add("size", ""+town.getTownChunks().size())
                    .add("leader", ""+town.getMayor().getName())
                    .add("health", ""+town.getHealth())
                    .add("max_health", ""+town.getMaxHealth())
                    .send(context.getSender());
            return null;
        }
        return Message.getMessage("command.failed.info-target-not-found");
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg()==1){
            List<String> ret = new ArrayList<>(Nation.nations.values().stream().map(Nation::getName).toList());
            ret.addAll(Town.towns.values().stream().map(Town::getName).toList());
//            ret.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            return ret.stream().filter(s->s.startsWith(context.getArg(0))).toList();
        }
        return null;
    }
}
