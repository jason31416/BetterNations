package cn.jason31416.betternations.command.resolution;

import cn.jason31416.planetlib.command.ICommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParentCommand;
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
