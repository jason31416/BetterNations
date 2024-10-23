package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleSender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface Message {
    Message add(String key, Object value);
    String toString();
    void send(CommandSender sender);
    default void send(SimpleSender sender){
        send(sender.sender());
    }
    default void send(SimplePlayer player){
        if(player.isOnline()) send(player.getPlayer());
    }
    void sendActionbar(Player sender);
    default void sendActionbar(SimplePlayer player){
        if(player.getPlayer() != null) sendActionbar(player.getPlayer());
    }
    default void broadcast(){
        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendMessage(toString());
        }
    }
    default void historicalBroadcast(){
        broadcast();
        // todo: save message to history record
    }
    public static Message getMessage(String key){
        return MessageLoader.getMessage(key);
    }
    public static Message getMessage(String key, String defaultValue){
        return MessageLoader.getMessage(key, defaultValue);
    }
    public static Message of(String content){
        return new StringMessage(content);
    }
}
