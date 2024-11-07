package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.item.*;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageList;
import cn.jason31416.planetlib.message.StringMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ItemCraftingManager {
    public static Map<ItemType, List<SimpleRecipe> > recipes=new HashMap<>();
    public static void unregisterAll(){
        Bukkit.clearRecipes();
        CustomItemType.itemTypes.clear();
    }
    public static void loadItems(ConfigurationSection section){
        for(String i: section.getKeys(false)){
            try{
                CustomItemType itemType = new CustomItemType(
                        i,
                        new StringMessage(section.getString(i+".name", "")).toString(),
                        Objects.requireNonNull(Material.getMaterial(section.getString(i+".material", "").toUpperCase()), "Material is not found!"),
                        new MessageList(section.getStringList(i+".lore")).asList(),
                        false
                );
                itemType.register();
            }catch (Exception e){
                Message.getMessage("admin.configuration-format-error").send(Bukkit.getConsoleSender());
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
                    SimpleCraftingRecipe recipe = new SimpleCraftingRecipe(productType.getItemStack(amount));
                    recipe.setShape(section.getStringList(i+".shape"));
                    ConfigurationSection sub = section.getConfigurationSection(i+".material");
                    if(sub == null) throw new RuntimeException("Lacking material section!");
                    for(String j: sub.getKeys(false)){
                        recipe.setMaterial(j, ItemType.getItemType(sub.getString(j, "")));
                    }
                    recipe.register();
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
                Message.getMessage("admin.configuration-format-error").send(Bukkit.getConsoleSender());
                e.printStackTrace();
            }
        }
    }
    public static void loadAll(){
        YamlConfiguration file;
        PlanetLib.instance.getLogger().info("\033[34mLoading items:\033[0m");
        try {
            file = YamlConfiguration.loadConfiguration(new File(BetterNations.instance.getDataFolder(), "items.yml"));
        }catch (Exception e){
            throw new RuntimeException("Unable to load items for BetterNations!");
        }
        if(file.isConfigurationSection("items")){
            loadItems(Objects.requireNonNull(file.getConfigurationSection("items")));
            BetterNations.instance.getLogger().info("\033[36m- Loaded "+file.getConfigurationSection("items").getKeys(false).size()+" items!\033[0m");
        }
        if(file.isConfigurationSection("recipes")){
            loadRecipes(Objects.requireNonNull(file.getConfigurationSection("recipes")));
            BetterNations.instance.getLogger().info("\033[36m- Loaded "+file.getConfigurationSection("recipes").getKeys(false).size()+" recipes!\033[0m");
        }
    }
}
