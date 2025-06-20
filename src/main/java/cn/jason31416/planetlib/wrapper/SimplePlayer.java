package cn.jason31416.planetlib.wrapper;

import cn.jason31416.betternations.army.states.ArmyCamp;
import cn.jason31416.betternations.army.states.InvasionFlag;
import cn.jason31416.betternations.army.states.SiegeFlag;
import cn.jason31416.betternations.army.states.StructuredArmy;
import cn.jason31416.betternations.manager.ArmyUpdateManager;
import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.NationalRank;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.nation.Relation;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.hook.VaultHook;
import cn.jason31416.planetlib.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record SimplePlayer(OfflinePlayer offlinePlayer) implements ConfigurationSerializable {
    public String getName() {
        return offlinePlayer.getName();
    }

    public UUID getUUID() {
        return offlinePlayer.getUniqueId();
    }

    public boolean isOnline() {
        return offlinePlayer.isOnline();
    }

    public SimpleLocation getLocation() {
        if(!isOnline()) throw new IllegalStateException("Player is not online");
        return SimpleLocation.of(getPlayer().getLocation());
    }

    public Player getPlayer() {
        if(!isOnline()) throw new IllegalStateException("Player is not online");
        return offlinePlayer.getPlayer();
    }
    public boolean hasPermission(Permission p){
        return getRank().hasPermission(p);
    }
    public boolean hasPermission(Permission p, SimpleLocation location){ // ONLY USE FOR BUILD AND STRUCTURE PERMISSIONS!
        if((p==Permission.STRUCTURE||p==Permission.BUILD)&& offlinePlayer.isOp()) return true;
        if((p==Permission.STRUCTURE||p==Permission.BUILD)&&StructuredArmy.armyLocationMap.containsKey(location.getChunkLocation())){
            for(StructuredArmy i: StructuredArmy.armyLocationMap.get(location.getChunkLocation())){
                if(i instanceof InvasionFlag || i instanceof SiegeFlag){
                    return false;
                }
            }
        }
        if(location.getChunkLocation().getNation()==null) return true;
        if(location.getChunkLocation().getTown()!=null) return location.getChunkLocation().getTown().getRole(this).hasPermission(p);
        return getRank().hasPermission(p)&&location.getChunkLocation().getNation().getRelation(getNation())==Relation.ALLY;
    }

    public void sendMessage(Message message) {
        if(!isOnline()) return;
        message.send(getPlayer());
    }
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut){
        if(!isOnline()) return;
        getPlayer().sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
    @Nullable
    public Nation getNation() {
        return Nation.playerNationMap.get(this);
    }
    public NationalRank getRank() {
        if(getNation() == null) return NationalRank.NONE;
        return getNation().getRank(this);
    }

    public double getBalance(){
        return VaultHook.getBalance(offlinePlayer);
    }
    public void addBalance(double amount){
        VaultHook.depositBalance(offlinePlayer, amount*Config.getDouble("multiplier.global-cost", 1.0));
    }
    public boolean withdrawBalance(double amount){
        return VaultHook.withdrawBalance(offlinePlayer, amount*Config.getDouble("multiplier.global-cost", 1.0)); // this is for multiplier support
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimplePlayer other)) {
            return false;
        }
        return offlinePlayer.getUniqueId().equals(other.offlinePlayer.getUniqueId());
    }

    public int hashCode() {
        return offlinePlayer.getUniqueId().hashCode();
    }

    public static SimplePlayer of(OfflinePlayer offlinePlayer) {
        return new SimplePlayer(offlinePlayer);
    }
    public static SimplePlayer of(Player player) {
        return new SimplePlayer(player);
    }
    public static SimplePlayer of(CommandSender player) {
        if(player instanceof Player) return new SimplePlayer((Player) player);
        return null;
    }
    public static SimplePlayer of(UUID uuid) {
        return new SimplePlayer(Bukkit.getOfflinePlayer(uuid));
    }
    @SuppressWarnings("deprecation")
    public static SimplePlayer of(String name) {
        return new SimplePlayer(Bukkit.getOfflinePlayer(name));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("uuid", getUUID().toString());
        return map;
    }
    @NotNull
    public static SimplePlayer deserialize(@NotNull Map<String, Object> map) {
        UUID uuid = UUID.fromString((String) map.get("uuid"));
        return new SimplePlayer(Bukkit.getOfflinePlayer(uuid));
    }
}
