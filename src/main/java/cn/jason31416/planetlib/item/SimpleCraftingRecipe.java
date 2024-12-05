package cn.jason31416.planetlib.item;

import cn.jason31416.planetlib.InvalidConfigurationException;
import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.message.StaticMessages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.*;

public class SimpleCraftingRecipe implements SimpleRecipe {
    public static class recipeListener implements Listener {
        @EventHandler
        public void preCrafting(PrepareItemCraftEvent event) {
            if(event.getInventory().getMatrix().length!=9) return;
            ItemType productType = ItemType.getItemType(event.getInventory().getResult());
            if(!recipes.containsKey(productType)){
                for(ItemStack i: event.getInventory().getMatrix()){
                    if(ItemType.getItemType(i) instanceof CustomItemType){
                        event.getInventory().setResult(null);
                        return;
                    }
                }
                return;
            }
            for (SimpleCraftingRecipe r : recipes.get(productType)) {
                for (int i = 0; i < 4 - r.recipe.size(); i++) {
                    for (int j = 0; j < 4 - r.recipe.get(0).size(); j++) {
                        outer:
                        {
                            for (int k = i; k < i + r.recipe.size(); k++) {
                                for (int l = j; l < j + r.recipe.get(0).size(); l++) {
                                    if(ItemType.getItemType(event.getInventory().getMatrix()[k * 3 + l]).getMaterial() != r.recipe.get(k - i).get(l - j).getMaterial()) {
                                        break outer;
                                    }
                                }
                            }
                            for (int k = i; k < i + r.recipe.size(); k++) {
                                for (int l = j; l < j + r.recipe.get(0).size(); l++) {
                                    if (!ItemType.getItemType(event.getInventory().getMatrix()[k * 3 + l]).equals(r.recipe.get(k - i).get(l - j))) {
                                        event.getInventory().setResult(null);
                                        break outer;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public static Map<ItemType, Set<SimpleCraftingRecipe> > recipes=new HashMap<>();
    public final List<List<ItemType>> recipe = new ArrayList<>();
    public final ItemStack result;
    public List<String> shape = null;
    public Map<String, ItemType> ingredients = new HashMap<>();
    public SimpleCraftingRecipe(ItemStack result){
        this.result = result;
    }
    public SimpleCraftingRecipe setShape(List<String> shape){
        this.shape = shape;
        return this;
    }
    public SimpleCraftingRecipe setMaterial(String c, ItemType type){
        ingredients.put(c, type);
        return this;
    }
    public void register(){
        ingredients.put(" ", new VanillaItemType(Material.AIR));
        recipe.clear();
        for(int i=0;i<shape.size();i++){ // what tf does this do
            recipe.add(new ArrayList<>());
            for(int j=0;j<shape.get(i).length();j++){
                if(!ingredients.containsKey(shape.get(i).substring(j, j+1))){
                    throw new InvalidConfigurationException("items.yml", "Unknown crafting material!");
                }
                recipe.get(i).add(ingredients.get(shape.get(i).substring(j, j+1)));
            }
        }
        if(!recipes.containsKey(ItemType.getItemType(result))) recipes.put(ItemType.getItemType(result), new HashSet<>());
        recipes.get(ItemType.getItemType(result)).add(this);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(PlanetLib.instance, UUID.randomUUID().toString()), result);
        recipe.shape(shape.toArray(new String[0]));
        for(String i: ingredients.keySet()){
            if(i.equals(" ")) continue;
            recipe.setIngredient(i.charAt(0), ingredients.get(i).getMaterial());
        }
        Bukkit.addRecipe(recipe);
    }
    public ItemStack getProduct(){
        return result;
    }
}
