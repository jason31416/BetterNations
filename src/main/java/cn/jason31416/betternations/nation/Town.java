package cn.jason31416.betternations.nation;

import cn.jason31416.betternations.structure.TownCore;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Town {
    // Static fields
    public static final Map<UUID, Town> towns = new HashMap<>();
    public static final Map<SimpleChunkLocation, Town> chunkTownMap = new HashMap<>();

    // Fields
    UUID id;
    String name;
    Nation nation;
    SimplePlayer mayor;
    TownCore core;
    Set<SimpleChunkLocation> townChunks, suburbanChunks;
    Map<SimplePlayer, TownRole> roles;
    // Constructors
    public Town(UUID id, String name, Nation nation) {
        this.id = id;
        this.name = name;
        this.nation = nation;
    }
    // Getter/Setters
    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Nation getNation() {
        return nation;
    }
    public SimplePlayer getMayor() {
        return mayor;
    }
    public TownCore getCore() {
        return core;
    }
    public Set<SimpleChunkLocation> getTownChunks() {
        return townChunks;
    }
    public TownRole getRole(SimplePlayer player) {
        if(!roles.containsKey(player)){
            return TownRole.NONE;
        }
        return roles.get(player);
    }
    public void setRole(SimplePlayer player, TownRole role) {
        if(role == TownRole.NONE){
            roles.remove(player);
            return;
        }
        roles.put(player, role);
    }
    // Methods
    public void registerTown() {
        towns.put(id, this);
    }
    public void unregisterTown() {
        towns.remove(id);
    }
    public void removeResident(SimplePlayer player){
        roles.remove(player);
    }
    public boolean claim(SimpleChunkLocation chunk){
        if(chunk.isClaimed()){
            return false;
        }
        suburbanChunks.add(chunk);
        return true;
    }
    private boolean unclaimConnectivityCheck(SimpleChunkLocation chunk){ // todo: town unclaiming method
        // todo: check if the chunk breaks connectivity
        return true;
    }
    public boolean claimAsTown(SimpleChunkLocation chunk){
        if(chunk.isTownChunk()||chunk.getNation() != nation) return false;
        boolean bb = false;
        for(SimpleChunkLocation adjacentChunk : chunk.getAdjacentChunks()){
            if(adjacentChunk.isTownChunk()&&adjacentChunk.getTown() == this){
                bb = true;
                break;
            }
        }
        if(!bb||chunk.getTown()==null||!chunk.getTown().suburbanChunks.contains(chunk)){
            return false;
        }
        chunk.getTown().suburbanChunks.remove(chunk);
        townChunks.add(chunk);
        return true;
    }
    // Static methods
    public static Town createTown(String name, SimpleLocation location, Nation nation, SimplePlayer mayor) {
        UUID id = UUID.randomUUID();
        Town town = new Town(id, name, nation);
        town.registerTown();
        town.mayor = mayor;
        nation.addTown(town);
        town.core = new TownCore(location, town);
        return town;
    }
    public static Town getTown(UUID id) {
        return towns.get(id);
    }
}
