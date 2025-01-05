package cn.jason31416.betternations.command.treaty;

import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

public class TreatyCommand extends ParentCommand {
    public TreatyCommand(IParentCommand parent) {
        super("treaty", parent);

        new TreatyCreateCommand(this);
        new TreatyInfoCommand(this);
        new TreatyAddtermCommand(this);
        new TreatyRemovetermCommand(this);
        new TreatySubmitCommand(this);
        new TreatySignCommand(this);
        new TreatyVetoCommand(this);
    }

    @Override
    public @Nullable Message executeRaw(ICommandContext context) {
        return null;
    }
}
