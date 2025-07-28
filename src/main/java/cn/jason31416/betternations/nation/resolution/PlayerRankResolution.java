package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.NationalRank;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;

import javax.annotation.Nonnull;

public class PlayerRankResolution extends PlayerResolution {
    public NationalRank rank;
    public PlayerRankResolution(Nation nation, SimplePlayer proposer, SimplePlayer target, NationalRank rank) {
        super(nation, proposer, target);
        this.rank = rank;
    }

    @Override
    public int importance() {
        if(rank == nation.getType().getOwnerRank()) return 5;
        if(nation.getRank(target).weight()>=500 || rank.weight() >= 500){
            return 3;
        }
        return 2;
    }

    @Override
    public void execute() {
        if(target.getNation() != nation) return;

        if(!nation.getType().allRanks.contains(rank)) return;

        if(target.getRank() == nation.getType().getOwnerRank()) return;

        if(rank == nation.getType().getOwnerRank()){
            nation.setRank(nation.getOwner(), nation.getType().getDefaultRank());
            nation.setOwner(target);
        }

        nation.setRank(target, rank);
    }

    @Override
    public @Nonnull Message getResolutionContent() {
        return Message.getMessage("nation.resolution.description.set-rank")
                .add("target", target.getName())
                .add("rank", rank.getDisplayName());
    }
}
