package cn.jason31416.betternations.nation;

import cn.jason31416.betternations.army.ArmorType;
import cn.jason31416.betternations.army.DamageSource;
import cn.jason31416.betternations.army.Damageable;
import cn.jason31416.betternations.structure.types.TownCore;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleWorld;

import java.util.*;

public class Town implements Damageable {
    // Static fields
    public static final Map<UUID, Town> towns = new HashMap<>();
    public static final Map<SimpleChunkLocation, Town> chunkTownMap = new HashMap<>();

    // Fields
    UUID id;
    String name;
    Nation nation;
    SimplePlayer mayor;
    public TownCore core;
    double townHealth=0;
    Set<SimpleChunkLocation> townChunks=new HashSet<>();
    Map<SimplePlayer, TownRole> roles=new HashMap<>();
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
        if(role == TownRole.MAYOR){
            roles.put(mayor, TownRole.MANAGER);
            mayor = player;
        }
        roles.put(player, role);
    }
    // Methods

    public void transferNation(Nation newNation){
        nation.towns.remove(this);
        nation = newNation;
        newNation.addTown(this);
        for(SimplePlayer player : roles.keySet()){
            if(player.getNation() != newNation){
                setRole(player, TownRole.NONE);
            }
        }
    }
    public void moveCore(SimpleLocation location){
        if(location.getChunkLocation().getTown() == this) {
            core.breakStructure();
            core = new TownCore();
            core.location = location;
            core.town = this;
            core.place();
        }
    }
    public void remove(){
        core.breakStructure();
        core.unregister();
        nation.towns.remove(this);
        for(SimpleChunkLocation chunk : townChunks) {
            chunkTownMap.remove(chunk);
        }
        unregisterTown();
    }
    public void registerTown() {
        towns.put(id, this);
    }
    public void unregisterTown() {
        towns.remove(id);
    }
    public void removeResident(SimplePlayer player){
        roles.remove(player);
    }
    public boolean claimChecks(SimpleChunkLocation chunk){
        boolean bb = false;
        for(SimpleChunkLocation adjacentChunk : chunk.getAdjacentChunks()){
            if(adjacentChunk.isTownChunk()&&adjacentChunk.getTown() == this){
                bb = true;
                break;
            }
        }
        return bb;
    }
    public boolean claim(SimpleChunkLocation chunk){
        if(chunk.isTownChunk()||chunk.getNation() != nation) return false;
        if(!claimChecks(chunk)) return false;
        if(!chunk.isClaimed()) {
            if(!nation.claim(chunk)) return false;
        }
        townChunks.add(chunk);
        chunkTownMap.put(chunk, this);
        return true;
    }
    private boolean isConnectedToCore(SimpleChunkLocation chunk, SimpleChunkLocation original, SimpleChunkLocation target){
        Queue<SimpleChunkLocation> chunks=new ArrayDeque<>();
        Set<SimpleChunkLocation> searched=new HashSet<>();
        searched.add(original);
        chunks.add(chunk);
        while(!chunks.isEmpty()){
            SimpleChunkLocation cur = chunks.poll();
            searched.add(cur);
            if(cur.equals(target)){
                return true;
            }
            for(SimpleChunkLocation c: cur.getAdjacentChunks()){
                if(c.getTown()==chunk.getTown()&&!searched.contains(c)){
                    chunks.add(c);
                }
            }
        }
        return false;
    }
    public boolean unclaimChecks(SimpleChunkLocation chunk){
        if(chunk.getTown()==null) return false;
        for(SimpleChunkLocation i: chunk.getAdjacentChunks()){
            if(i.getTown()==chunk.getTown()&&!isConnectedToCore(i, chunk, chunk.getTown().getCore().location.getChunkLocation())){
                return false;
            }
        }
        return true;
    }
    public boolean unclaim(SimpleChunkLocation chunk){
        if(!chunk.isTownChunk()) return false;
        townChunks.remove(chunk);
        chunkTownMap.remove(chunk);
        return true;
    }
    // Data storage
    public boolean serialize(IDataItem dataItem){
        dataItem.setUUID(id);
        dataItem.set("name", name);
        dataItem.set("mayor", mayor.getUUID().toString());
        dataItem.set("nation", nation.getId().toString());
        dataItem.set("hp", townHealth);
        List<String> townChunkList = new ArrayList<>(),
                roleList = new ArrayList<>();
        String lstWorld = "";
        for(SimpleChunkLocation chunk : townChunks) {
            String worldID = chunk.world().getBukkitWorld().getUID().toString();
            if(!lstWorld.isEmpty()&&lstWorld.equals(worldID)){
                townChunkList.add(chunk.x() + "_" + chunk.z());
            }else {
                townChunkList.add(chunk.x() + "_" + chunk.z() + "_" + worldID);
                lstWorld = worldID;
            }
        }
        for(SimplePlayer player : roles.keySet()) {
            roleList.add(player.getUUID().toString()+":"+roles.get(player).name());
        }
        dataItem.set("chunks", String.join(";", townChunkList));
        dataItem.set("roles", String.join(";", roleList));
        dataItem.set("isCapital", (nation.capital==this)?1:0);
        return true;
    }
    public static Town deserialize(IDataItem dataItem){
        UUID id = dataItem.getUUID();
        String name = dataItem.getString("name");
        Nation nation = Nation.getNation(UUID.fromString(dataItem.getString("nation")));
        SimplePlayer mayor = SimplePlayer.of(UUID.fromString(dataItem.getString("mayor")));
        Town town = new Town(id, name, nation);
        town.townHealth = dataItem.getDouble("hp");
        town.mayor = mayor;
        String[] townChunks = dataItem.getString("chunks").split(";");
        SimpleWorld world = null;
        for(String chunk : townChunks) {
            if(chunk.isEmpty()) continue;
            String[] chunkLocation = chunk.split("_");
            SimpleChunkLocation c;
            if(world != null&&chunkLocation.length == 2){
                c = SimpleChunkLocation.of(Integer.parseInt(chunkLocation[0]), Integer.parseInt(chunkLocation[1]), world);
            }else {
                c = SimpleChunkLocation.of(Integer.parseInt(chunkLocation[0]), Integer.parseInt(chunkLocation[1]), SimpleWorld.of(UUID.fromString(chunkLocation[2])));
                world = c.world();
            }
            town.townChunks.add(c);
            chunkTownMap.put(c, town);
        }
        String[] roleList = dataItem.getString("roles").split(";");
        for(String role : roleList) {
            if(role.isEmpty()) continue;
            String[] roleInfo = role.split(":");
            SimplePlayer player = SimplePlayer.of(UUID.fromString(roleInfo[0]));
            TownRole townRole = TownRole.valueOf(roleInfo[1]);
            town.roles.put(player, townRole);
        }
        nation.towns.add(town);
        if(dataItem.get("isCapital").equals(1)){
            nation.capital = town;
        }
        town.registerTown();
        return town;
    }
    // Static methods
    public static Town createTown(String name, SimpleLocation location, Nation nation, SimplePlayer mayor) {
        if(!location.getChunkLocation().isClaimed()) nation.claim(location.getChunkLocation());
        if(location.getChunkLocation().getNation()!=nation) return null;
        UUID id = UUID.randomUUID();
        Town town = new Town(id, name, nation);
        town.registerTown();
        town.townHealth=10;
        town.mayor = mayor;
        town.setRole(mayor, TownRole.MAYOR);
        nation.addTown(town);
        town.core = new TownCore();
        town.core.location = location.getBlockLocation();
        town.core.town = town;
        town.core.place();
        town.townChunks.add(location.getChunkLocation());
        chunkTownMap.put(location.getChunkLocation(), town);
        return town;
    }
    public static Town getTown(UUID id) {
        return towns.get(id);
    }
    public static Town getTown(String name){
        for(Town town : towns.values()){
            if(town.getName().equals(name)){
                return town;
            }
        }
        return null;
    }

    @Override
    public double getHealth() {
        return townHealth;
    }

    @Override
    public void damage(double dmg) {
        townHealth -= dmg;
    }
    @Override
    public void damage(DamageSource dmg){
        townHealth -= dmg.getDamageTowards(ArmorType.TERRITORY);
    }

    @Override
    public boolean isAlive() {
        return townHealth>0;
    }
}
