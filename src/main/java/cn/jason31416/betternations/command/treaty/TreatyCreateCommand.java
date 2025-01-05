package cn.jason31416.betternations.command.treaty;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.nation.treaty.Treaty;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.message.StringMessage;
import net.wesjd.anvilgui.AnvilGUI;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TreatyCreateCommand extends ChildCommand {

    public TreatyCreateCommand(IParentCommand parent) {
        super("create", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.getSender().isPlayer()||!context.checkArgs(ParameterType.STRING)) return null;
        if(context.getPlayer().getNation()==null) return Message.getMessage("command.failed.player-not-in-nation");
        String name = context.getArg(0);
        if(Treaty.treatyMap.containsKey(name)) return Message.getMessage("command.failed.name-already-exists");
        double cost = Config.getDouble("treaty.creation-cost");
        if(!context.getPlayer().withdrawBalance(cost)) return Message.getMessage("command.failed.not-enough-money").add("amount", cost);
        Treaty treaty = new Treaty(name, context.getPlayer());
        return Message.getMessage("command.success.treaty-created").add("treaty", treaty.name).add("id", treaty.id);
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return List.of("<name>");
    }
}
