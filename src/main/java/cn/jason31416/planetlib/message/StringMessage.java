package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StringMessage implements Message {
    public static BukkitAudiences bukkitAudiences;
    public static MiniMessage miniMessage = MiniMessage.miniMessage();
    String content;
    public StringMessage(String content) {
        this.content = content;
    }
    public StringMessage add(String placeholder, Object value){
        content = content.replace("%"+placeholder+"%", (value instanceof String)?(String)value:value.toString());
        return this;
    }
    public String toString(){
        return ChatColor.translateAlternateColorCodes('&', InternalPlaceholder.replacePlaceholders(content, null));
    }
    public void send(CommandSender player){
        String message = ChatColor.translateAlternateColorCodes('&', InternalPlaceholder.replacePlaceholders(content, null));
        Component component = miniMessage.deserialize(message);
        bukkitAudiences.sender(player).sendMessage(miniMessage.deserialize(InternalPlaceholder.replacePlaceholders(content, null)));
    }
    public void sendActionbar(Player player){
        bukkitAudiences.player(player).sendActionBar(miniMessage.deserialize(InternalPlaceholder.replacePlaceholders(content, SimplePlayer.of(player))));
    }
}
