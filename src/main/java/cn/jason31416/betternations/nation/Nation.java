package cn.jason31416.betternations.nation;

import cn.jason31416.betternations.nation.resolution.AbstractResolution;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Color;

import java.util.*;

public class Nation {
    // Static fields
    public static Map<UUID, Nation> nations = new java.util.HashMap<>();
    public static Map<SimplePlayer, Nation> playerNationMap = new java.util.HashMap<>();
    // Fields
    public final Map<UUID, AbstractResolution> resolutions = new HashMap<>();

    List<Town> towns = new ArrayList<>();
    Map<SimplePlayer, NationalRank> memberRanks = new HashMap<>();
    UUID id;
    String name;
    SimplePlayer owner;
    Town capital;
    Color color;
    NationType type;
    Map<Nation, Relation> relations = new HashMap<>();
    // Constructors
    public Nation(UUID id, String name, Color color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
    // Getters and setters
    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public void rename(String name) {
        this.name = name;
    }
    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }
    public Town getCapital() {
        return capital;
    }
    public void setCapital(Town capital) {
        this.capital = capital;
    }
    public List<Town> getTowns() {
        return towns;
    }
    public SimplePlayer getOwner() {
        return owner;
    }
    public NationType getType() {
        return type;
    }
    public Set<SimplePlayer> getMembers() {
        return memberRanks.keySet();
    }
    public NationalRank getRank(SimplePlayer player) {
        return memberRanks.get(player);
    }
    public void setRank(SimplePlayer player, NationalRank rank) {
        memberRanks.put(player, rank);
    }
    // Methods
    public void setOwner(SimplePlayer owner) {
        if(this.owner!= null){
            memberRanks.put(this.owner, type.allRanks.get(type.allRanks.size() - 2));
        }
        this.owner = owner;
        memberRanks.put(owner, type.getOwnerRank());
    }
    public void kickPlayer(SimplePlayer player) {
        if(player == owner){
            return;
        }
        memberRanks.remove(player);
        playerNationMap.remove(player);
        List<Town> ownedTowns = new ArrayList<>();
        for(Town town : towns){
            if(town.getMayor() == player){
                ownedTowns.add(town);
            }else{
                town.removeResident(player);
            }
        }
        if(!ownedTowns.isEmpty()){
            // Create a new nation for the kicked player
            String initname = Message.getMessage("town.city-state.default-name").add("town_name", ownedTowns.get(0).name).toString(), name=initname;
            int cnt=1;
            while(getNation(initname)!=null){
                initname = name+"_"+(cnt++);
            }
            Nation newNation = createNation(player, initname, ownedTowns.get(0));
            for(Town town : ownedTowns){
                if(newNation.getTowns().contains(town)) town.nation = newNation;
                else newNation.addTown(town);
            }
            // todo: broadcast historical event message
            newNation.setRelation(this, Relation.ENEMY);
            // todo: war declaration message
        }
    }
    public boolean claim(SimpleChunkLocation chunk){
        if(chunk.isClaimed()) return false;
        Town closestTown = null;
        double closestDistance = Double.MAX_VALUE;
        for(Town town : towns){
            double distance = chunk.distance(town.getCore().location.getChunkLocation());
            if(distance < closestDistance){
                closestTown = town;
                closestDistance = distance;
            }
        }
        if(closestTown == null) return false;
        return closestTown.claim(chunk);
    }
    public void registerNation() {
        nations.put(id, this);
    }
    public void unregisterNation() {
        nations.remove(id);
    }
    public void addTown(Town town) {
        towns.add(town);
        town.nation = this;
    }
    public void setRelation(Nation other, Relation relation){
        if(relation == null) return;
        if(relation == Relation.NEUTRAL){
            relations.remove(other);
            other.relations.remove(this);
        }else{
            relations.put(other, relation);
            other.relations.put(this, relation);
        }
    }
    public Relation getRelation(Nation other){
        if(!relations.containsKey(other)) return Relation.NEUTRAL;
        return relations.get(other);
    }

    // Static methods
    public static Nation createNation(SimplePlayer player, String name) {
        Random colorRandomizer = new Random();
        int r = colorRandomizer.nextInt(256);
        int g = colorRandomizer.nextInt(256);
        int b = colorRandomizer.nextInt(256);
        Nation nation = new Nation(UUID.randomUUID(), name, Color.fromRGB(r, g, b));
        nation.capital = Town.createTown(Config.getString("nation.capital-name").replace("%nation%", name), player.getLocation(), nation, player);
        nation.registerNation();
        nation.setOwner(player);
        return nation;
    }
    public static Nation createNation(SimplePlayer player, String name, Town capital) {
        Random colorRandomizer = new Random();
        int r = colorRandomizer.nextInt(256);
        int g = colorRandomizer.nextInt(256);
        int b = colorRandomizer.nextInt(256);
        Nation nation = new Nation(UUID.randomUUID(), name, Color.fromRGB(r, g, b));
        nation.capital = capital;
        nation.registerNation();
        nation.setOwner(player);
        return nation;
    }
    public static Nation getNation(UUID id) {
        return nations.get(id);
    }
    public static Nation getNation(String name) {
        for (Nation nation : nations.values()) {
            if (nation.getName().equals(name)) {
                return nation;
            }
        }
        return null;
    }
}
