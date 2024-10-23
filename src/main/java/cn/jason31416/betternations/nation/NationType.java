package cn.jason31416.betternations.nation;

import cn.jason31416.betternations.nation.resolution.AbstractResolution;
import cn.jason31416.betternations.nation.resolution.DailyResolution;
import cn.jason31416.betternations.nation.resolution.ImportantResolution;
import cn.jason31416.planetlib.message.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum NationType {
    MONARCHY("Monarchy", new ArrayList<>(List.of(
            new NationalRank("Peasant", Set.of(Permission.SUBURB_BUILD)),
            new NationalRank("Knight", Set.of(Permission.SUBURB_BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.NATION_STRUCTURE)),
            new NationalRank("General", Set.of(Permission.SUBURB_BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.NATION_STRUCTURE)),
            new NationalRank("King", Set.of(Permission.SUBURB_BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.NATION_STRUCTURE))
    )), (resolution) -> {
        if(!resolution.proposer.getRank().hasPermission(Permission.CHANGE_NATION_ATTRIBUTE)) return false;
        if(resolution instanceof DailyResolution){
            resolution.setRequiredSigners(List.of(NationalRank.getRank("King"), NationalRank.getRank("General")));
            resolution.setRequiredRatio(0.5);
        }else{
            resolution.setRequiredSigners(List.of(NationalRank.getRank("King")));
            resolution.setRequiredRatio(1);
        }
        return true;
    }),
    DEMOCRACY("Democracy", new ArrayList<>(List.of(
            new NationalRank("Member", Set.of(Permission.SUBURB_BUILD)),
            new NationalRank("Citizen", Set.of(Permission.SUBURB_BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.NATION_STRUCTURE)),
            new NationalRank("President", Set.of(Permission.SUBURB_BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.NATION_STRUCTURE))
    )), (resolution) -> {
        if(resolution.proposer.getRank() == NationalRank.getRank("Member")) return false;
        resolution.setRequiredSigners(List.of(NationalRank.getRank("Citizen"), NationalRank.getRank("President")));
        resolution.setRequiredRatio(0.5);
        return true;
    }),
    REPUBLIC("Republic", new ArrayList<>(List.of(
            new NationalRank("Subject", Set.of(Permission.SUBURB_BUILD)),
            new NationalRank("Representative", Set.of(Permission.SUBURB_BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.NATION_STRUCTURE)),
            new NationalRank("President", Set.of(Permission.SUBURB_BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.NATION_STRUCTURE))
    )), (resolution) -> {
        if (resolution.proposer.getRank() == NationalRank.getRank("Subject")) {
            return false;
        }
        if (resolution instanceof ImportantResolution) {
            // todo: if important resolution
            resolution.setRequiredSigners(List.of(NationalRank.getRank("President"))); // President must sign to pass important resolutions such as war declaration
            resolution.setRequiredRatio(1);
        } else if (resolution instanceof DailyResolution){
            resolution.setRequiredSigners(List.of(NationalRank.getRank("Representative"), NationalRank.getRank("President")));
            resolution.setRequiredRatio(0);
            resolution.setMinimalSigners(2); // As long as two or more representatives sign, the resolution can pass
        }else{
            resolution.setRequiredSigners(List.of(NationalRank.getRank("Representative"), NationalRank.getRank("President")));
            resolution.setRequiredRatio(0.5); // If half of representatives sign, the resolution can pass
        }
        return true;
    });

    public interface DecisionMaker {
        boolean makeDecision(AbstractResolution resolution);
    }

    public final String name;
    public final List<NationalRank> allRanks;
    public final DecisionMaker decisionMaker;
    NationType(String name, List<NationalRank> ranks, DecisionMaker decisionMaker){
        this.allRanks = ranks;
        this.name = name;
        this.decisionMaker = decisionMaker;
    }
    public NationalRank getOwnerRank() {
        return allRanks.get(allRanks.size() - 1);
    }
    public NationalRank getDefaultRank() {
        return allRanks.get(0);
    }
    public String getDisplayName() {
        return Message.getMessage("nation.type."+name.toLowerCase()+".name").toString();
    }
    public String getDescription() {
        return Message.getMessage("nation.type."+name.toLowerCase()+".description").toString();
    }
}
