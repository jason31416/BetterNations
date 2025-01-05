package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.manager.HistoricalBroadcastManager;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nonnull;
import java.util.List;

public class RenameResolution extends AbstractResolution implements DailyResolution {
    String name;
    public RenameResolution(Nation nation, SimplePlayer proposer, String newName) {
        super(nation, proposer);
        name = newName;
    }

    @Override
    public void execute() {
        HistoricalBroadcastManager.broadcast(Message.getMessage("history.nation-rename")
                        .add("nation", nation.getName())
                        .add("new_name", name),
                List.of(nation));
        nation.rename(name);
    }

    @Override
    public @Nonnull Message getResolutionContent() {
        return Message.getMessage("nation.resolution.description.rename-nation")
                .add("name", nation.getName()).add("new_name", name);
    }
}
