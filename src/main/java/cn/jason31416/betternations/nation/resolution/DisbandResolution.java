package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.manager.HistoricalBroadcastManager;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nonnull;
import java.util.List;

public class DisbandResolution extends AbstractResolution {
    public DisbandResolution(Nation nation, SimplePlayer proposer) {
        super(nation, proposer);
    }

    @Override
    public int importance() {
        return 5;
    }

    @Override
    public void execute() {
        nation.disband();
        HistoricalBroadcastManager.broadcast(Message.getMessage("history.nation-disband")
                .add("nation", nation.getName()),
                List.of(nation));
    }

    @Override
    public @Nonnull Message getResolutionContent() {
        return Message.getMessage("nation.resolution.description.disband-nation")
                .add("nation", nation.getName());
    }
}
