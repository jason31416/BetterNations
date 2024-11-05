package cn.jason31416.betternations.command.resolution;

import cn.jason31416.betternations.command.town.TownClaimCommand;
import cn.jason31416.betternations.command.town.TownUnclaimCommand;
import cn.jason31416.planetlib.command.*;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

public class ResolutionCommand extends ParentCommand {
    private final ICommand listCommand;
    public ResolutionCommand(IParentCommand parent) {
        super("resolution", parent);

        new ResolutionInfoCommand(this);
        new ResolutionAcceptCommand(this);
        new ResolutionDeclineCommand(this);
        listCommand = new ResolutionListCommand(this);
    }

    @Override
    public @Nullable Message executeRaw(ICommandContext context) {
        return listCommand.execute(context);
    }
}
