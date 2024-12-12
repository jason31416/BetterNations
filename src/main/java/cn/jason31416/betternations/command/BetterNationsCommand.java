package cn.jason31416.betternations.command;

import cn.jason31416.betternations.command.admin.AdminCommand;
import cn.jason31416.betternations.command.admin.ReloadCommand;
import cn.jason31416.betternations.command.misc.BNFSCKCommand;
import cn.jason31416.betternations.command.misc.CraftingGuideCommand;
import cn.jason31416.betternations.command.nation.*;
import cn.jason31416.betternations.command.resolution.ResolutionCommand;
import cn.jason31416.betternations.command.town.TownCommand;
import cn.jason31416.betternations.command.town.TownTeleportCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.RootCommand;
import cn.jason31416.planetlib.message.Message;
import org.jetbrains.annotations.Nullable;

public class BetterNationsCommand extends RootCommand {
    public static BetterNationsCommand instance;
    public BetterNationsCommand() {
        super("nation");

        new NationCreateCommand(this);
        new NationDisbandCommand(this);
        new NationClaimCommand(this);
        new NationUnclaimCommand(this);
        new NationJoinCommand(this);
        new NationLeaveCommand(this);
        new NationRankCommand(this);
        new NationColorCommand(this);
        new NationKickCommand(this);
        new NationDeclareWarCommand(this);
        new NationRenameCommand(this);
        new NationMapCommand(this);
        new ToggleArmyUpdateCommand(this);
        new BNFSCKCommand(this);

        new TownTeleportCommand(this);

        new CraftingGuideCommand(this);

        new TownCommand(this);
        new ResolutionCommand(this);
        new AdminCommand(this);
        new ReloadCommand(this);
        instance = this;
    }

    @Override
    public @Nullable Message execute(ICommandContext context) {
        return null;
    }
}
