package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.manager.HistoricalBroadcastManager;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.NationType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nonnull;
import java.util.List;

public class ChangeTypeResolution extends AbstractResolution {

    NationType type;
    public ChangeTypeResolution(Nation nation, SimplePlayer proposer, NationType type) {
        super(nation, proposer);
        this.type=type;
    }

    @Override
    public int importance() {
        return 5;
    }

    @Override
    public void execute() {
        nation.setType(type);
    }

    @Override
    public @Nonnull Message getResolutionContent() {
        return Message.getMessage("nation.resolution.description.change-type")
                .add("nation", nation.getName())
                .add("type", type.getDisplayName());
    }
}
