package cn.jason31416.betternations.nation;

import cn.jason31416.betternations.army.ArmorType;
import cn.jason31416.betternations.army.DamageSource;
import cn.jason31416.betternations.army.Damageable;
import cn.jason31416.betternations.manager.map.MapDisplayManager;
import cn.jason31416.betternations.structure.types.TownCore;
import cn.jason31416.betternations.structure.types.TownRuin;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.message.StaticMessages;
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
    public SimplePlayer mayor;
    public double devPoints=0;
    public Map<SimplePlayer, Integer> devadded=new HashMap<>();
    public TownCore core;
    public double townHealth=0;
    Set<SimpleChunkLocation> townChunks=new HashSet<>();
    public Map<SimplePlayer, TownRole> roles=new HashMap<>();
    // Constructors
    public Town(UUID id, String name, Nation nation) {
        this.id = id;
        this.name = name;
        this.nation = nation;
    }
    // Getter/Setters
    public TownLevel getLevel(){
        for(int i=1;i<TownLevel.townLevels.size();i++){
            if(devPoints < TownLevel.townLevels.get(i).points){
                return TownLevel.townLevels.get(i-1);
            }
        }
        return TownLevel.townLevels.get(TownLevel.townLevels.size()-1);
    }
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
        if (role == TownRole.MAYOR) {
            for (SimplePlayer p: roles.keySet()) {
                if (roles.get(p) == TownRole.MAYOR) {
                    roles.put(p, TownRole.MANAGER);
                }
            }
            mayor = player;
        }
        roles.put(player, role);
    }
    // Methods

    public void transferNation(Nation newNation){
        nation.towns.remove(this);
        for(SimpleChunkLocation i: new HashSet<>(townChunks)){
            nation.forceUnclaim(i);
            newNation.claim(i);
        }
        nation = newNation;
        newNation.addTown(this);
        for(SimplePlayer player : new HashSet<>(roles.keySet())){
            if(player.getNation() != newNation){
                setRole(player, TownRole.NONE);
            }
        }
        setRole(newNation.owner, TownRole.MAYOR);
        mayor=newNation.owner;
        core.updateHologram();
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
        if(core != null) {
            core.breakStructure();
            core.unregister();
        }
        townHealth=0;
        nation.towns.remove(this);
        for(SimpleChunkLocation chunk : townChunks) {
            chunkTownMap.remove(chunk);
        }
        MapDisplayManager.updateTown(this);
        unregisterTown();
        if (Config.getBoolean("town.require-ruin")) TownRuin.create(core.location, name);
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
    public synchronized boolean claim(SimpleChunkLocation chunk){
        if(chunk.isTownChunk()||chunk.getNation() != nation) return false;
        if(!claimChecks(chunk)) return false;
        if(!chunk.isClaimed()) {
            if(!nation.claim(chunk)) return false;
        }
        townChunks.add(chunk);
        chunkTownMap.put(chunk, this);
        MapDisplayManager.updateTown(this);
        return true;
    }
    private boolean isConnectedToCore(SimpleChunkLocation chunk, SimpleChunkLocation original, SimpleChunkLocation target){
        Queue<SimpleChunkLocation> chunks=new ArrayDeque<>();
        Set<SimpleChunkLocation> searched=new HashSet<>();
        searched.add(original);
        chunks.add(chunk);
        searched.add(chunk);
        while(!chunks.isEmpty()){
            SimpleChunkLocation cur = chunks.poll();
            if(cur.equals(target)){
                return true;
            }
            for(SimpleChunkLocation c: cur.getAdjacentChunks()){
                if(c.getTown()==chunk.getTown()&&!searched.contains(c)){
                    chunks.add(c);
                    searched.add(c);
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
    public synchronized boolean unclaim(SimpleChunkLocation chunk){
        if(!chunk.isTownChunk()) return false;
        townChunks.remove(chunk);
        chunkTownMap.remove(chunk);
        MapDisplayManager.updateTown(this);
        return true;
    }
    // Data storage
    public boolean serialize(IDataItem dataItem){
        dataItem.setUUID(id);
        dataItem.set("name", name);
        dataItem.set("mayor", mayor.getUUID().toString());
        dataItem.set("nation", nation.getId().toString());
        dataItem.set("devpoints", devPoints);
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
        return true;
    }
    public static Town deserialize(IDataItem dataItem){
        UUID id = dataItem.getUUID();
        String name = dataItem.getString("name");
        Nation nation = Nation.getNation(UUID.fromString(dataItem.getString("nation")));
        SimplePlayer mayor = SimplePlayer.of(UUID.fromString(dataItem.getString("mayor")));
        Town town = new Town(id, name, nation);
        town.townHealth = dataItem.getDouble("hp");
        if(mayor.getNation() != nation) mayor = nation.getOwner();
        town.mayor = mayor;
        town.devPoints = dataItem.getDouble("devpoints");
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
            if(c.getNation()==nation) {
                town.townChunks.add(c);
                chunkTownMap.put(c, town);
            }
        }
        String[] roleList = dataItem.getString("roles").split(";");
        for(String role : roleList) {
            if(role.isEmpty()) continue;
            String[] roleInfo = role.split(":");
            SimplePlayer player = SimplePlayer.of(UUID.fromString(roleInfo[0]));
            TownRole townRole = TownRole.valueOf(roleInfo[1]);
            if(!player.equals(mayor)&&townRole==TownRole.MAYOR){
                town.roles.put(player, TownRole.NONE);
            }else if(!nation.getMembers().contains(player)&&townRole!=TownRole.GREENCARD){
                town.roles.put(player, TownRole.NONE);
            }else{
                town.roles.put(player, townRole);
            }
        }
        town.roles.put(mayor, TownRole.MAYOR);
        nation.towns.add(town);
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
        MapDisplayManager.updateTown(town);
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
    public void rename(String newName){
        this.name = newName;
        if(core!=null) core.hologram.setText(core.getHologramText());
    }

    @Override
    public double getHealth() {
        return townHealth;
    }
    public double getMaxHealth(){
        return Config.getDouble("combat.chunk-hp")*townChunks.size();
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
