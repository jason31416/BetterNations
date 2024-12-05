package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.PlanetLib;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageLoader {
    public static MessageLoader instance;
    public ConfigurationSection messageConfig;
    public MessageLoader(ConfigurationSection messageConfig) {
        this.messageConfig = messageConfig;
    }
    public MessageLoader(File filePath) {
        try{
            this.messageConfig = YamlConfiguration.loadConfiguration(filePath);
        }catch (Exception ignored){
            throw new RuntimeException("Failed to load message config file!");
        }
    }
    public StringMessage getStringMessage(String key, String defaultMessage) {
        if(messageConfig.isList(key)){
            return new StringMessage(String.join("\n", messageConfig.getStringList(key)));
        }
        if(messageConfig.contains(key)) return new StringMessage(messageConfig.getString(key));
        return defaultMessage == null? null : new StringMessage(defaultMessage);
    }
    public String getRawMessage(String key, String defaultMessage) {
        if(messageConfig.contains(key)) return messageConfig.getString(key);
        return defaultMessage;
    }
    public List<String> getStringList(String key, List<String> defaultList) {
        if(messageConfig.isList(key)) return messageConfig.getStringList(key);
        return defaultList;
    }
    public static MessageList getList(String key){
        if(instance == null) throw new RuntimeException("Planetlib not initialized!");
        return new MessageList(instance.getStringList(key, new ArrayList<>()));
    }
    public static MessageList getList(String key, List<String> defaultList){
        if(instance == null) throw new RuntimeException("Planetlib not initialized!");
        return new MessageList(instance.getStringList(key, defaultList));
    }
    public static Message getMessage(String key) {
        if(instance == null) throw new RuntimeException("Planetlib not initialized!");
        return instance.getStringMessage(key, "<red>Error: message "+key+" not found, please contact admin!");
    }
    public static Message getMessage(String key, String defaultMessage) {
        if(instance == null) throw new RuntimeException("Planetlib not initialized!");
        return instance.getStringMessage(key, defaultMessage);
    }
    public static void initialize(File filePath, JavaPlugin plugin){
        PlanetLib.instance.getLogger().info("\033[34mLoading messages: "+filePath.getName()+"\033[0m");
        instance = new MessageLoader(filePath);
        StringMessage.bukkitAudiences = BukkitAudiences.create(plugin);
        StringMessage.miniMessage = MiniMessage.miniMessage();
    }
    public static void close(){
        StringMessage.bukkitAudiences.close();
    }
}
