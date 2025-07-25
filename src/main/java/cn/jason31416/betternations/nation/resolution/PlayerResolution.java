package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

public abstract class PlayerResolution extends AbstractResolution {
    public SimplePlayer target;
    public PlayerResolution(Nation nation, SimplePlayer proposer, SimplePlayer target) {
        super(nation, proposer);
        this.target = target;
    }
}
