package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nonnull;

public class DeclareWarResolution extends NationResolution implements ImportantResolution {
    public DeclareWarResolution(Nation nation, SimplePlayer proposer, Nation otherNation) {
        super(nation, proposer, otherNation);
    }

    @Override
    public void execute() {
        if(nation.getRelation(otherNation)!= Relation.ENEMY) {
            nation.setRelation(otherNation, Relation.ENEMY);
            // todo: System message about war declaration
        }
    }

    @Override @Nonnull
    public Message getResolutionContent() {
        return Message.getMessage("nation.resolution.description.declare-war")
                .add("from", nation.getName())
                .add("to", otherNation.getName());
    }
}
