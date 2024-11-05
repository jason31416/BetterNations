package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nonnull;

public class DisbandResolution extends AbstractResolution implements ImportantResolution {

    public DisbandResolution(Nation nation, SimplePlayer proposer) {
        super(nation, proposer);
    }

    @Override
    public void execute() {
        nation.disband();
    }

    @Override
    public @Nonnull Message getResolutionContent() {
        return Message.getMessage("nation.resolution.description.disband-nation")
                .add("nation", nation.getName());
    }
}
