package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nonnull;

public class KickPlayerResolution extends PlayerResolution {
    public KickPlayerResolution(Nation nation, SimplePlayer proposer, SimplePlayer target) {
        super(nation, proposer, target);
    }

    @Override
    public void execute() {
        nation.kickPlayer(target);
    }

    @Override
    public @Nonnull Message getResolutionContent() {
        return Message.getMessage("nation.resolution.description.kick-player")
                .add("nation", nation.getName())
                .add("target", target.getName());
    }
}
