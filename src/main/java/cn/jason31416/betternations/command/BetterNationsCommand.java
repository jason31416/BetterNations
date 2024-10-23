package cn.jason31416.betternations.command;

import cn.jason31416.betternations.command.nation.NationCreateCommand;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.RootCommand;
import cn.jason31416.planetlib.message.Message;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BetterNationsCommand extends RootCommand {
    public BetterNationsCommand() {
        super("nation");

        new NationCreateCommand(this);
    }

    @Override
    public @Nullable Message execute(ICommandContext context) {
        return null;
    }
}
