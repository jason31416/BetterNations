package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

public class KickPlayerResolution extends PlayerResolution {
    public KickPlayerResolution(Nation nation, SimplePlayer proposer, SimplePlayer target) {
        super(nation, proposer, target);
    }

    @Override
    public void execute() {
        nation.kickPlayer(target);
    }

    @Override
    public String getResolutionContent() {
        return null;
    }
}
