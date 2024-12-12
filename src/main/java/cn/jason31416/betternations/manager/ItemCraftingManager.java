package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.planetlib.InvalidConfigurationException;
import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.item.*;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageList;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.message.StringMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.util.*;

public class ItemCraftingManager {
    public static class ItemCategory {
        public final String displayName, catid;
        public final Material material;
        public final List<CustomItemType> types=new ArrayList<>();
        public ItemCategory(String id, String name, Material mat){
            displayName = name;
            material = mat;
            catid = id;
        }
    }
    public static Map<ItemType, List<SimpleRecipe> > recipes=new HashMap<>();
    public static Map<String, ItemCategory> itemTypes=new HashMap<>();
    public static void unregisterAll(){
        Bukkit.clearRecipes();
        itemTypes.clear();
        recipes.clear();
        CustomItemType.itemTypes.clear();
    }
    public static void loadItemCategories(ConfigurationSection section){
        for(String i: section.getKeys(false)){
            try{
                ItemCategory c = new ItemCategory(i, section.getString(i+".name"), Objects.requireNonNull(Material.getMaterial(section.getString(i + ".material", "").toUpperCase()), "Material is not found!"));
                itemTypes.put(i, c);
            }catch (Exception e){
                Message.getMessage("admin.configuration-format-error-with-loc").add("file", "items").add("line", "Item "+i).send(Bukkit.getConsoleSender());
                e.printStackTrace();
            }
        }
    }
    public static void loadItems(ConfigurationSection section){
        for(String i: section.getKeys(false)){
            try{
                CustomItemType itemType;
                if(section.contains(i + ".skull")) {
                    itemType = new CustomItemType(
                            i,
                            new StringMessage(section.getString(i + ".name", "")).toString(),
                            section.getString(i + ".skull", ""),
                            new MessageList(section.getStringList(i + ".lore")).asList()
                    );
                }else{
                    itemType = new CustomItemType(
                            i,
                            new StringMessage(section.getString(i + ".name", "")).toString(),
                            Objects.requireNonNull(Material.getMaterial(section.getString(i + ".material", "").toUpperCase()), "Material is not found!"),
                            new MessageList(section.getStringList(i + ".lore")).asList(),
                            false
                    );
                }
                if(section.contains(i+".model")) itemType.setCustomModelData(section.getInt(i+".model"));
                if(section.contains(i+".glowing")) itemType.glow = true;
                if(section.contains(i+".category")){
                    if(itemTypes.containsKey(section.getString(i+".category"))){
                        itemTypes.get(section.getString(i+".category")).types.add(itemType);
                    }else throw new InvalidConfigurationException("item.yml", i+".category");
                }
                itemType.register();
            }catch (Exception e){
                Message.getMessage("admin.configuration-format-error-with-loc").add("file", "items").add("line", "Item "+i).send(Bukkit.getConsoleSender());
                e.printStackTrace();
            }
        }
    }
    public static void loadRecipes(ConfigurationSection section){
        for(String i: section.getKeys(false)){
            try{
                if(section.getString(i+".type", "crafting").equalsIgnoreCase("crafting")){
                    ItemType productType = ItemType.getItemType(section.getString(i+".product", ""));
                    int amount = section.getInt(i+".product-count", 1);
                    SimpleCraftingRecipe recipe = new SimpleCraftingRecipe(productType, amount);
                    recipe.setShape(section.getStringList(i+".shape"));
                    ConfigurationSection sub = section.getConfigurationSection(i+".material");
                    if(sub == null) throw new RuntimeException("Lacking material section!");
                    for(String j: sub.getKeys(false)){
                        recipe.setMaterial(j, ItemType.getItemType(sub.getString(j, "")));
                    }
                    recipe.register(i);
                    List<SimpleRecipe> allrec = recipes.get(productType);
                    if(allrec==null) allrec=new ArrayList<>();
                    allrec.add(recipe);
                    recipes.put(productType, allrec);
                }else if(section.getString(i+".type", "crafting").equalsIgnoreCase("smelting")){
                    ItemType productType = ItemType.getItemType(section.getString(i+".product", ""));
                    int amount = section.getInt(i+".product-count", 1);
                    SimpleFurnaceRecipe recipe= new SimpleFurnaceRecipe(
                            productType.getItemStack(amount),
                            ItemType.getItemType(section.getString(i+".ingredient", "")),
                            (float) section.getDouble(i+".exp", 0),
                            section.getInt(i+".time")
                    );
                    recipe.register();
                    List<SimpleRecipe> allrec = recipes.get(productType);
                    if(allrec==null) allrec=new ArrayList<>();
                    allrec.add(recipe);
                    recipes.put(productType, allrec);
                }else{
                    throw new RuntimeException("Unrecognized recipe type!");
                }
            }catch (Exception e){
                Message.getMessage("admin.configuration-format-error-with-loc").add("file", "items").add("line", "Recipe "+i).send(Bukkit.getConsoleSender());
                e.printStackTrace();
            }
        }
    }
    public static void loadAll(){
        PlanetLib.instance.getLogger().info("\033[34mLoading items:\033[0m");
        File dir = new File(BetterNations.instance.getDataFolder(), "item");
        if(!dir.exists()){
            dir.mkdir();
        }
        for(File file: dir.listFiles()){
            if(file.isFile() && file.getName().endsWith(".yml")){
                YamlConfiguration fl;
                try {
                    fl = YamlConfiguration.loadConfiguration(file);
                }catch (Exception e){
                    throw new RuntimeException("Unable to load items for BetterNations!");
                }
                if(fl.isConfigurationSection("categories")){
                    loadItemCategories(Objects.requireNonNull(fl.getConfigurationSection("categories")));
                    BetterNations.instance.getLogger().info("\033[36m- Loaded "+fl.getConfigurationSection("categories").getKeys(false).size()+" categories from "+file.getName()+"!\033[0m");
                }
            }
        }
        for(File file: dir.listFiles()){
            if(file.isFile() && file.getName().endsWith(".yml")){
                YamlConfiguration fl;
                try {
                    fl = YamlConfiguration.loadConfiguration(file);
                }catch (Exception e){
                    throw new RuntimeException("Unable to load items for BetterNations!");
                }
                if(fl.isConfigurationSection("items")){
                    loadItems(Objects.requireNonNull(fl.getConfigurationSection("items")));
                    BetterNations.instance.getLogger().info("\033[36m- Loaded "+fl.getConfigurationSection("items").getKeys(false).size()+" items from "+file.getName()+"!\033[0m");
                }
            }
        }
        for(File file: dir.listFiles()){
            if(file.isFile() && file.getName().endsWith(".yml")){
                YamlConfiguration fl;
                try {
                    fl = YamlConfiguration.loadConfiguration(file);
                }catch (Exception e){
                    throw new RuntimeException("Unable to load items for BetterNations!");
                }
                if(fl.isConfigurationSection("recipes")){
                    loadRecipes(Objects.requireNonNull(fl.getConfigurationSection("recipes")));
                    BetterNations.instance.getLogger().info("\033[36m- Loaded "+fl.getConfigurationSection("recipes").getKeys(false).size()+" recipes "+file.getName()+"!\033[0m");
                }
            }
        }
    }
}
