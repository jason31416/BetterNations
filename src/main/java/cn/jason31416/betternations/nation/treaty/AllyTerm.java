package cn.jason31416.betternations.nation.treaty;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.planetlib.message.Message;

public class AllyTerm extends Term {
    public AllyTerm(Nation from, Nation target) {
        super(from, target);
    }

    @Override
    public void execute() {
        from.setRelation(target, Relation.ALLY);
    }
    @Override
    public String getDescription() {
        return Message.getMessage("treaty.term.ally").add("from", from.getName()).add("target", target.getName()).toString();
    }
}
