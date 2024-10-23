package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

public class RevokeAllianceResolution extends NationResolution {
    public RevokeAllianceResolution(Nation nation, SimplePlayer proposer, Nation otherNation) {
        super(nation, proposer, otherNation);
    }

    @Override
    public void execute() {
        if(nation.getRelation(otherNation)!= Relation.NEUTRAL) {
            nation.setRelation(otherNation, Relation.NEUTRAL);
            // todo: System message about revoke alliance
        }
    }

    @Override
    public String getResolutionContent() {
        return null;
    }
}
