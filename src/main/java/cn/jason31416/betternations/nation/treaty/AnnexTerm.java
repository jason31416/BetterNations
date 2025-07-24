package cn.jason31416.betternations.nation.treaty;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import java.util.ArrayList;

public class AnnexTerm extends Term {
    public AnnexTerm(Nation from, Nation target) {
        super(from, target);
    }
    @Override
    public void execute() { // target -> from
        for(SimpleChunkLocation chunkLocation : target.nationalChunks){
            from.nationalChunks.add(chunkLocation);
            Nation.chunkNationMap.put(chunkLocation, from);
        }
        for(Town town: target.getTowns()){
            from.addTown(town);
        }
        for(SimplePlayer player : target.getMembers()){
            Nation.playerNationMap.put(player, from);
            from.memberRanks.put(player, from.getType().getDefaultRank());
        }
        for(Nation other : new ArrayList<>(target.relations.keySet())){
            other.setRelation(target, Relation.NEUTRAL);
        }
        target.unregisterNation();
    }
    @Override
    public String getDescription() {
        return Message.getMessage("treaty.term.annex").add("from", from.getName()).add("target", target.getName()).toString();
    }
}
