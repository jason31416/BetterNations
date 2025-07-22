package cn.jason31416.betternations.nation;

import cn.jason31416.betternations.manager.map.MapDisplayManager;
import cn.jason31416.betternations.nation.resolution.AbstractResolution;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleWorld;
import org.bukkit.Color;

import java.util.*;

public class Nation {
    // Static fields
    public static Map<UUID, Nation> nations = new java.util.HashMap<>();
    public static Map<SimplePlayer, Nation> playerNationMap = new java.util.HashMap<>();
    public static Map<SimpleChunkLocation, Nation> chunkNationMap = new java.util.HashMap<>();
    // Fields
    public final Map<String, AbstractResolution> resolutions = new HashMap<>();

    List<Town> towns = new ArrayList<>();
    public Set<SimpleChunkLocation> nationalChunks = new HashSet<>();
    Map<SimplePlayer, NationalRank> memberRanks = new HashMap<>();
    UUID id;
    String name;
    SimplePlayer owner;
    Color color;
    NationType type=NationType.MONARCHY;
    public Map<Nation, Relation> relations = new HashMap<>();
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
    public String getColorTag(){
        return "<#"+Integer.toHexString(color.asRGB())+">";
    }
    public void setColor(Color color) {
        this.color = color;
        for(Town t: towns){
            if(t.core!=null) t.core.hologram.setText(t.core.getHologramText());
        }
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
    public void setType(NationType type) {
        for(SimplePlayer player : memberRanks.keySet()){
            if(player.equals(owner)) memberRanks.put(player, type.getOwnerRank());
            else memberRanks.put(player, type.getDefaultRank());
        }
        this.type = type;
    }
    public Set<SimplePlayer> getMembers() {
        return memberRanks.keySet();
    }
    public NationalRank getRank(SimplePlayer player) {
        return memberRanks.get(player);
    }
    public boolean exists(){
        return nations.containsValue(this);
    }
    public boolean isNomad(){
        return towns.isEmpty();
    }
    public void disband() {
        for(Town town : new ArrayList<>(towns)){
            town.remove();
        }
        for(SimpleChunkLocation chunk : nationalChunks){
            chunkNationMap.remove(chunk);
        }
        for(Nation other : new ArrayList<>(relations.keySet())){
            other.setRelation(this, Relation.NEUTRAL);
        }
        for(SimplePlayer player : memberRanks.keySet()){
            playerNationMap.remove(player);
        }
        unregisterNation();
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
            String initname = Message.getMessage("town.revolt-name").add("town_name", ownedTowns.get(0).name).toString(), name=initname;
            int cnt=1;
            while(getNation(initname)!=null){
                initname = name+"_"+(cnt++);
            }
            Nation newNation = createNation(player, initname);
            for(Town town : ownedTowns){
                if(newNation.getTowns().contains(town)) town.nation = newNation;
                else newNation.addTown(town);
            }
            newNation.setRelation(this, Relation.ENEMY);
            // todo: war declaration message
        }
    }
    public void removePlayer(SimplePlayer player){
        if(player == owner){
            return;
        }
        memberRanks.remove(player);
        playerNationMap.remove(player);
        for(Town town : towns){
            town.setRole(owner, TownRole.MAYOR);
            town.removeResident(player);
        }
    }
    public void addPlayer(SimplePlayer player) {
        if(player.getNation()!=null) return;
        for(Town town: towns){
            if(town.getRole(player)==TownRole.GREENCARD) town.setRole(player, TownRole.RESIDENT);
        }
        playerNationMap.put(player, this);
        memberRanks.put(player, type.getDefaultRank());
    }
    public synchronized boolean claim(SimpleChunkLocation chunk){
        if(chunk.isClaimed()) return false;
//        if(!claimChecks(chunk)) return false;
        nationalChunks.add(chunk);
        chunkNationMap.put(chunk, this);
        MapDisplayManager.updateNation(this);
        return true;
    }
    public synchronized boolean unclaim(SimpleChunkLocation chunk){
        if(!nationalChunks.contains(chunk)) return false;
        if(chunk.isTownChunk()){
            Town town = chunk.getTown();
            if(town!=null){
                town.unclaim(chunk);
            }
        }
        nationalChunks.remove(chunk);
        chunkNationMap.remove(chunk);
        MapDisplayManager.updateNation(this);
        return true;
    }
    public synchronized boolean forceUnclaim(SimpleChunkLocation chunk){ // Note that this method is unsafe, use carefully
        if(!nationalChunks.contains(chunk)) return false;
        nationalChunks.remove(chunk);
        chunkNationMap.remove(chunk);
        MapDisplayManager.updateNation(this);
        return true;
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
        if(other==this) return Relation.ALLY;
        if(other==null) return Relation.NEUTRAL;
        if(!relations.containsKey(other)) return Relation.NEUTRAL;
        return relations.get(other);
    }
    // Data storage
    public boolean serialize(IDataItem dataItem){
        dataItem.setUUID(id);
        dataItem.set("name", name);
        dataItem.set("owner", owner.getUUID().toString());
        dataItem.set("color", color.asRGB());
        dataItem.set("type", type.name());
        ArrayList<String> relationList = new ArrayList<>(), memberList = new ArrayList<>(), nationalChunkList = new ArrayList<>();
        for(Nation other : relations.keySet()){
            relationList.add(other.getId().toString() + ":" + relations.get(other).name());
        }
        for(SimplePlayer player : memberRanks.keySet()){
            memberList.add(player.getUUID().toString() + ":" + memberRanks.get(player).name());
        }
        String lstWorld = "";
        for(SimpleChunkLocation chunk : nationalChunks) {
            String worldID = chunk.world().getBukkitWorld().getUID().toString();
            if(!lstWorld.isEmpty()&&lstWorld.equals(worldID)){
                nationalChunkList.add(chunk.x() + "_" + chunk.z());
            }else {
                nationalChunkList.add(chunk.x() + "_" + chunk.z() + "_" + worldID);
                lstWorld = worldID;
            }
        }
        dataItem.set("relations", String.join(";", relationList));
        dataItem.set("members", String.join(";", memberList));
        dataItem.set("chunks", String.join(";", nationalChunkList));
        return true;
    }
    public static Nation deserialize(IDataItem dataItem){ // NOTE THAT TOWNS MUST BE LOADED AFTER NATIONS
        Nation nation = new Nation(dataItem.getUUID(), dataItem.getString("name"), Color.fromRGB(dataItem.getInteger("color")));
        nation.owner = SimplePlayer.of(UUID.fromString(dataItem.getString("owner")));
        nation.type = NationType.valueOf(dataItem.getString("type"));
        nation.relations.clear();
        for(String relationStr : dataItem.getString("relations").split(";")) {
            if (relationStr.isEmpty()) continue;
            String[] relationArr = relationStr.split(":");
            Nation other = getNation(UUID.fromString(relationArr[0]));
            if (other != null) {
                nation.setRelation(other, Relation.valueOf(relationArr[1]));
            }
        }
        SimpleWorld world = null;
        for(String chunk : dataItem.getString("chunks").split(";")) {
            if(chunk.isEmpty()) continue;
            String[] chunkLocation = chunk.split("_");
            SimpleChunkLocation c;
            if(world != null&&chunkLocation.length == 2){
                c = SimpleChunkLocation.of(Integer.parseInt(chunkLocation[0]), Integer.parseInt(chunkLocation[1]), world);
            }else {
                c = SimpleChunkLocation.of(Integer.parseInt(chunkLocation[0]), Integer.parseInt(chunkLocation[1]), SimpleWorld.of(UUID.fromString(chunkLocation[2])));
                world = c.world();
            }
            nation.nationalChunks.add(c);
            chunkNationMap.put(c, nation);
        }
        nation.memberRanks.clear();
        for(String memberStr : dataItem.getString("members").split(";")) {
            if (memberStr.isEmpty()) continue;
            String[] memberArr = memberStr.split(":");
            SimplePlayer player = SimplePlayer.of(UUID.fromString(memberArr[0]));
            if(nation.getType().getOwnerRank()==NationalRank.getRank(memberArr[1])&&!nation.owner.equals(player)){
                nation.memberRanks.put(player, nation.getType().getDefaultRank());
            }else {
                nation.memberRanks.put(player, NationalRank.getRank(memberArr[1]));
            }
            playerNationMap.put(player, nation);
        }
        nation.registerNation();
        return nation;
    }
    // Static methods
    public static Nation createNation(SimplePlayer player, SimpleLocation location, String name, String townName) {
        if(location.getChunkLocation().isClaimed()) return null;
        Nation nation = createNation(player, name);
        Town.createTown((townName==null?Config.getString("nation.capital-name").replace("%nation%", name):townName), location, nation, player);
        return nation;
    }
    public static Nation createNation(SimplePlayer player, String name) {
        Random colorRandomizer = new Random();
        int r = colorRandomizer.nextInt(256);
        int g = colorRandomizer.nextInt(256);
        int b = colorRandomizer.nextInt(256);
        Nation nation = new Nation(UUID.randomUUID(), name, Color.fromRGB(r, g, b));
        nation.registerNation();
        nation.setOwner(player);
        playerNationMap.put(player, nation);
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
