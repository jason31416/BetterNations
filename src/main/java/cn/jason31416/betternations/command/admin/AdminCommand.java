package cn.jason31416.betternations.command.admin;

import cn.jason31416.betternations.manager.UnclaimableRegionManager;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParentCommand;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

public class AdminCommand extends ParentCommand {
    public AdminCommand(IParentCommand parent) {
        super("admin", parent);

        new GiveCommand(this);
        new ReloadCommand(this);
        new ExecuteCommand(this);
        new TownRuinCommand(this);
        new SetNaturalResourceCommand(this);
        new ArmyUpdateCommand(this);
        new ToggleUnclaimableCommand(this);
    }

    @Override
    public @Nullable Message executeRaw(ICommandContext context) {
        return null;
    }
}
