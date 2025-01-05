package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.manager.HistoricalBroadcastManager;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.nation.treaty.Treaty;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nonnull;
import java.util.List;

public class SignTreatyResolution extends AbstractResolution implements ImportantResolution {
    Treaty treaty;
    public SignTreatyResolution(Nation nation, SimplePlayer proposer, Treaty treaty) {
        super(nation, proposer);
        this.treaty = treaty;
    }

    @Override
    public void execute() {
        treaty.sign(nation);
    }

    @Override @Nonnull
    public Message getResolutionContent() {
        return Message.getMessage("nation.resolution.description.sign-treaty")
                .add("nation", nation.getName())
                .add("treaty", treaty.name)
                .add("treaty_id", treaty.id.toString());
    }
}
