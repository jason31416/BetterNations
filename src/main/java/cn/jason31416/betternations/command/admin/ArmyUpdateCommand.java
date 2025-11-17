package cn.jason31416.betternations.command.admin;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.manager.ArmyUpdateManager;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArmyUpdateCommand extends ChildCommand {
    public ArmyUpdateCommand(IParentCommand parent) {
        super(List.of("reload"), parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(!context.getSender().sender().isOp()) return Message.getMessage("command.failed.no-permission");
        context.getSender().sendMessage(Message.getMessage("admin.army-updating"));
        new BukkitRunnable() {
            @Override
            public void run() {
                ArmyUpdateManager.run();
            }
        }.runTaskAsynchronously(BetterNations.instance);
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
