package cn.jason31416.betternations.nation;

import cn.jason31416.betternations.nation.resolution.*;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum NationType {
    AUTOCRACY("Autocracy", new ArrayList<>(List.of(
            new NationalRank("Peasant", 10, Set.of(Permission.BUILD)),
            new NationalRank("Officer", 400, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.STRUCTURE, Permission.MANAGE_ARMY)),
            new NationalRank("Dictator", 1000, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY))
    )), (resolution) -> {
        if(!(resolution instanceof OutsiderResolution)&&!resolution.proposer.getRank().hasPermission(Permission.CHANGE_NATION_ATTRIBUTE)) return false;
        resolution.setRequiredSigners(List.of(NationalRank.getRank("dictator")));
        resolution.setRequiredRatio(1);
        return true;
    }),
    MONARCHY("Monarchy", new ArrayList<>(List.of(
            new NationalRank("Peasant", 10, Set.of(Permission.BUILD)),
            new NationalRank("Knight", 100, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.STRUCTURE, Permission.MANAGE_ARMY)),
            new NationalRank("General", 500, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY)),
            new NationalRank("King", 1000, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY))
    )), (resolution) -> {
        if(!(resolution instanceof OutsiderResolution)&&!resolution.proposer.getRank().hasPermission(Permission.CHANGE_NATION_ATTRIBUTE)) return false;
        if(resolution instanceof DailyResolution){
            resolution.setRequiredSigners(List.of(NationalRank.getRank("king"), NationalRank.getRank("general")));
            resolution.setMinimalSigners(2); // As long as two or more kings or generals sign, the resolution can pass
            resolution.setRequiredRatio(0);
        }else{
            resolution.setRequiredSigners(List.of(NationalRank.getRank("king")));
            resolution.setRequiredRatio(1);
        }
        return true;
    }),
    DEMOCRACY("Democracy", new ArrayList<>(List.of(
            new NationalRank("Member", 10, Set.of(Permission.BUILD)),
            new NationalRank("Citizen", 900, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY)),
            new NationalRank("Leader", 1000, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY))
    )), (resolution) -> {
        if(!(resolution instanceof OutsiderResolution)&&resolution.proposer.getRank() == NationalRank.getRank("member")) return false;
        resolution.setRequiredSigners(List.of(NationalRank.getRank("citizen"), NationalRank.getRank("leader")));
        resolution.setRequiredRatio(0.5);
        return true;
    }),
    ANARCHY("Anarchy", new ArrayList<>(List.of(
            new NationalRank("Member", 10, Set.of(Permission.BUILD)),
            new NationalRank("Citizen", 900, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY)),
            new NationalRank("Leader", 1000, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY))
    )), (resolution) -> {
        if(!(resolution instanceof OutsiderResolution)&&resolution.proposer.getRank() == NationalRank.getRank("member")) return false;
        resolution.setRequiredSigners(List.of(NationalRank.getRank("citizen"), NationalRank.getRank("leader")));
        resolution.setRequiredRatio(0);
        resolution.setMinimalSigners(1);
        return true;
    }),
    REPUBLIC("Republic", new ArrayList<>(List.of(
            NationalRank.getRank("Member"),
            new NationalRank("Representative", 900, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY)),
            new NationalRank("President", 1000, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY))
    )), (resolution) -> {
        if (!(resolution instanceof OutsiderResolution)&&resolution.proposer.getRank() == NationalRank.getRank("member")) {
            return false;
        }
        if(resolution instanceof DisbandResolution){
            List<SimplePlayer> signers = new ArrayList<>();
            signers.add(resolution.nation.owner);
            for(Town i: resolution.nation.getTowns()){
                signers.add(i.getMayor());
            }
            resolution.addRequiredSigners(signers); // President & Town leaders must sign to disband a nation
            resolution.setRequiredRatio(1);
        }else if (resolution instanceof ImportantResolution) {
            resolution.setRequiredSigners(List.of(NationalRank.getRank("president"))); // President must sign to pass important resolutions such as war declaration
            resolution.setRequiredRatio(1);
        } else if (resolution instanceof DailyResolution){
            resolution.setRequiredSigners(List.of(NationalRank.getRank("representative"), NationalRank.getRank("president")));
            resolution.setRequiredRatio(0);
            resolution.setMinimalSigners(2); // As long as two or more representatives sign, the resolution can pass
        }else{
            resolution.setRequiredSigners(List.of(NationalRank.getRank("representative"), NationalRank.getRank("president")));
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
