package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

public abstract class NationResolution extends AbstractResolution {
    public Nation otherNation;
    public NationResolution(Nation nation, SimplePlayer proposer, Nation otherNation) {
        super(nation, proposer);
        this.otherNation = otherNation;
    }
}
