package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.NationalRank;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

public class PlayerRankResolution extends PlayerResolution {
    NationalRank rank;
    public PlayerRankResolution(Nation nation, SimplePlayer proposer, SimplePlayer target, NationalRank rank) {
        super(nation, proposer, target);
        this.rank = rank;
    }

    @Override
    public void execute() {
        if(target.getNation() != nation) return;
        nation.setRank(target, rank);
    }

    @Override
    public String getResolutionContent() {
        return null;
    }
}
