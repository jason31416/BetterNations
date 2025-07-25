package cn.jason31416.betternations.nation;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.nation.resolution.*;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public enum NationType {
    AUTOCRACY("Autocracy", new ArrayList<>(List.of(
            new NationalRank("Peasant", 10, Set.of(Permission.BUILD, Permission.STRUCTURE)),
            new NationalRank("Officer", 400, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.STRUCTURE, Permission.MANAGE_ARMY)),
            new NationalRank("Dictator", 1000, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY))
    )), (resolution) -> {
        return resolution.signedPlayers.contains(resolution.nation.owner);
    }),
    MONARCHY("Monarchy", new ArrayList<>(List.of(
            new NationalRank("Peasant", 10, Set.of(Permission.BUILD, Permission.STRUCTURE)),
            new NationalRank("Knight", 100, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.STRUCTURE, Permission.MANAGE_ARMY)),
            new NationalRank("General", 500, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY)),
            new NationalRank("King", 1000, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY))
    )), (resolution) -> {
        if(resolution.importance() >= 3) return resolution.signedPlayers.contains(resolution.nation.owner);
        return checkCount(resolution, getAllPlayersOfRanks(resolution.nation,
                List.of(NationalRank.getRank("King"), NationalRank.getRank("General"))), 1);
    }),
    DEMOCRACY("Democracy", new ArrayList<>(List.of(
            new NationalRank("Member", 10, Set.of(Permission.BUILD, Permission.STRUCTURE)),
            new NationalRank("Citizen", 900, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY)),
            new NationalRank("Leader", 1000, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY))
    )), (resolution) -> {
        if(resolution.importance() <= 1) return checkCount(resolution, getAllPlayersOfRanks(resolution.nation,
                List.of(NationalRank.getRank("Citizen"), NationalRank.getRank("Leader"))), 1);
        return checkRatio(resolution, getAllPlayersOfRanks(resolution.nation,
                List.of(NationalRank.getRank("Citizen"), NationalRank.getRank("Leader"))), 0.5);
    }),
    ANARCHY("Anarchy", new ArrayList<>(List.of(
            new NationalRank("Member", 10, Set.of(Permission.BUILD, Permission.STRUCTURE)),
            new NationalRank("Citizen", 900, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY)),
            new NationalRank("Leader", 1000, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY))
    )), (resolution) -> {
        if(resolution.importance() == 5) return resolution.signedPlayers.contains(resolution.nation.owner);
        return checkCount(resolution, getAllPlayersOfRanks(resolution.nation,
                List.of(NationalRank.getRank("Citizen"), NationalRank.getRank("Leader"))), 1);
    }),
    REPUBLIC("Republic", new ArrayList<>(List.of(
            NationalRank.getRank("Member"),
            new NationalRank("Representative", 900, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY)),
            new NationalRank("President", 1000, Set.of(Permission.BUILD, Permission.NATION_CLAIM, Permission.NATION_UNCLAIM, Permission.CHANGE_NATION_ATTRIBUTE, Permission.STRUCTURE, Permission.CHANGE_RANK, Permission.TOWN_CREATE, Permission.MANAGE_ARMY))
    )), (resolution) -> {
        if(resolution instanceof PlayerRankResolution && ((PlayerRankResolution) resolution).rank == NationalRank.getRank("President")) return checkRatio(resolution, getAllPlayersOfRanks(resolution.nation,
                List.of(NationalRank.getRank("Representative"), NationalRank.getRank("President"))), 0.5);
        if(resolution.importance() >= 4) return resolution.signedPlayers.contains(resolution.nation.owner);
        if(resolution.importance() == 3) return checkCount(resolution, getAllPlayersOfRanks(resolution.nation,
                List.of(NationalRank.getRank("Representative"))), 1) && resolution.signedPlayers.contains(resolution.nation.owner);
        return checkCount(resolution, getAllPlayersOfRanks(resolution.nation,
                List.of(NationalRank.getRank("Representative"), NationalRank.getRank("President"))), 1);
    });

    private static List<SimplePlayer> getAllPlayersOfRanks(Nation nation, List<NationalRank> ranks){
        List<SimplePlayer> ret = new ArrayList<>();
        for(SimplePlayer player: nation.getMembers()){
            if(ranks.contains(player.getRank())){
                ret.add(player);
            }
        }
        return ret;
    }
    private static boolean checkRatio(AbstractResolution resolution, List<SimplePlayer> requiredSigners, double ratio){
        int count = 0;
        for(SimplePlayer player: requiredSigners){
            if(resolution.signedPlayers.contains(player)){
                count++;
            }
        }
        return count >= requiredSigners.size() * ratio;
    }
    private static boolean checkCount(AbstractResolution resolution, List<SimplePlayer> requiredSigners, int count){
        int cnt = 0;
        for(SimplePlayer player: requiredSigners){
            if(resolution.signedPlayers.contains(player)){
                cnt++;
            }
        }
//        BetterNations.instance.getLogger().info("checkCount: requiredSigners: "+requiredSigners+", count: "+count+", cnt: "+cnt);
        return cnt >= count || cnt == requiredSigners.size();
    }

    public interface DecisionMaker {
        boolean checkPass(AbstractResolution resolution);
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
