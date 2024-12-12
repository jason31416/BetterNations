package cn.jason31416.betternations.command.nation;

import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ToggleArmyUpdateCommand extends ChildCommand {
    public static BossBar bossBar=null;
    public ToggleArmyUpdateCommand(IParentCommand parent) {
        super("nextupdate", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        if(!bossBar.getPlayers().contains(context.getPlayer().getPlayer())){
            bossBar.addPlayer(context.getPlayer().getPlayer());
        } else{
            bossBar.removePlayer(context.player().getPlayer());
        }
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
