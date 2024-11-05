package cn.jason31416.betternations.command.town;

import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

public class TownCommand extends ParentCommand {
    public TownCommand(IParentCommand parent) {
        super("town", parent);

        new TownClaimCommand(this);
        new TownUnclaimCommand(this);
    }

    @Override
    public @Nullable Message executeRaw(ICommandContext context) {
        return null;
    }
}
