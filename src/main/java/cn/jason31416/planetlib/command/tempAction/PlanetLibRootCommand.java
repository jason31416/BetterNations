package cn.jason31416.planetlib.command.tempAction;

import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.RootCommand;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.List;

public class PlanetLibRootCommand extends RootCommand {

    public PlanetLibRootCommand() {
        super("planetlib");
        new ExecuteActionCommand(this);
    }

    @Override @Nullable
    public Message execute(ICommandContext context) {
        context.sender().sendMessage(StaticMessages.NO_API_COMMAND);
        return null;
    }
}
