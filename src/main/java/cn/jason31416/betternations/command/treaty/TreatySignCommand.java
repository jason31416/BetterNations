package cn.jason31416.betternations.command.treaty;

import cn.jason31416.betternations.nation.resolution.SignTreatyResolution;
import cn.jason31416.betternations.nation.treaty.Treaty;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TreatySignCommand extends ChildCommand {

    public TreatySignCommand(IParentCommand parent) {
        super("sign", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(!context.getSender().isPlayer()) return null;
        Treaty treaty = Treaty.treatyMap.get(context.getArg(0));
        if(treaty==null) return Message.getMessage("command.failed.invalid-treaty");
        if(context.getPlayer().getNation()==null) return Message.getMessage("command.failed.player-not-in-nation");
        if(treaty.state!=Treaty.TreatyState.PROPOSED||!treaty.getAffected().contains(context.getPlayer().getNation())) return Message.getMessage("command.failed.treaty-cannot-sign");
        new SignTreatyResolution(context.getPlayer().getNation(), context.getPlayer(), treaty).propose();
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
