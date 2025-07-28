package cn.jason31416.planetlib;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class Config {
    public static FileConfiguration config;
    public static JavaPlugin plugin;

    public static void start(JavaPlugin pl) {
        config = pl.getConfig();
        plugin = pl;

//        try(InputStream fis = plugin.getClass().getClassLoader().getResourceAsStream("config.yml")){
//            if(fis == null) {
//                throw new RuntimeException("Failed to load default config file.");
//            }
//            defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(fis, StandardCharsets.UTF_8));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
    public static FileConfiguration getFile(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }
    public static FileConfiguration getConfig() {
        return config;
    }
//    public static void attemptUpdateConfigValue(String path) {
//        plugin.getLogger().info(path+" "+config.contains(path));
//        if(!config.contains(path)) {
//            plugin.reloadConfig();
//            config = plugin.getConfig();
//            config.set(path, defaultConfig.get(path));
//            plugin.saveConfig();
//
//            plugin.getLogger().warning("Found & updated missing config entry: " + path + " = " + defaultConfig.get(path));
//        }
//    }

    public static Object get(String path) {
        return config.get(path);
    }
    public static Object get(String path, Object def){
        return config.get(path, def);
    }
    public static String getString(String path){
        return config.getString(path);
    }
    public static String getString(String path, String def){
        return config.getString(path, def);
    }
    public static int getInt(String path){
        return config.getInt(path);
    }
    public static int getInt(String path, int def){
        return config.getInt(path, def);
    }
    public static double getDouble(String path){
        return config.getDouble(path);
    }
    public static double getDouble(String path, double def){
        return config.getDouble(path, def);
    }
    public static boolean getBoolean(String path){
        return getBoolean(path, false);
    }
    public static boolean getBoolean(String path, boolean def){
        return config.getBoolean(path, def);
    }

    public static ItemStack getItemStack(String path){
        ConfigurationSection confSec = config.getConfigurationSection(path);
        if(confSec == null) return null;
        Material material = Material.getMaterial(confSec.getString("material", "").toUpperCase());
        if(material == null) return null;
        ItemStack itemStack = new ItemStack(material, confSec.getInt("amount", 1));
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) return null;
        itemMeta.setDisplayName(confSec.getString("name", null));
        itemMeta.setLore(confSec.getStringList("lore"));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
    public static boolean contains(String path){
        return config.contains(path);
    }
    public static Set<String> getKeys(@Nullable String path){
        if(path == null||path.isEmpty()) {
            return config.getKeys(false);
        }
        ConfigurationSection confSec = config.getConfigurationSection(path);
        if(confSec == null) {
            return new HashSet<>();
        }
        return confSec.getKeys(false);
    }
}
