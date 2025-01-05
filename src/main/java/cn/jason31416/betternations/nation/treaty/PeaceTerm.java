package cn.jason31416.betternations.nation.treaty;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.planetlib.message.Message;

public class PeaceTerm extends Term {
    public PeaceTerm(Nation from, Nation target) {
        super(from, target);
    }
    @Override
    public void execute() {
        from.setRelation(target, Relation.NEUTRAL);
    }
    @Override
    public String getDescription() {
        return Message.getMessage("treaty.term.peace").add("from", from.getName()).add("target", target.getName()).toString();
    }
}
