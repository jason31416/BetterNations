package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MessageList implements Message {
    private final List<String> content;
    public MessageList(List<String> content) {
        this.content = content;
    }
    public MessageList add(String placeholder, Object value){
        content.replaceAll(s -> s.replace("%"+placeholder+"%", value.toString()));
        return this;
    }

    @Override
    public void send(CommandSender sender) {
        for (String s : content) {
            new StringMessage(s).send(sender);
        }
    }

    @Override
    public void sendActionbar(Player sender) {
        throw new UnsupportedOperationException("Cannot send list as actionbar!");
    }

    public List<String> asList() {
        ArrayList<String> list = new ArrayList<>(content);
        list.replaceAll(s -> new StringMessage(s).toString());
        return list;
    }
}
