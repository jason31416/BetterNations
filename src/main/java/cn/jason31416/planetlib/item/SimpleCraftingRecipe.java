package cn.jason31416.planetlib.item;

import cn.jason31416.planetlib.PlanetLib;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.*;

public class SimpleCraftingRecipe implements SimpleRecipe{
    public final ShapedRecipe recipe;
    public final Map<String, ItemType> ingredients=new HashMap<>();
    public SimpleCraftingRecipe(ItemStack result){
        recipe = new ShapedRecipe(new NamespacedKey(PlanetLib.instance, UUID.randomUUID().toString()), result);
    }
    public SimpleCraftingRecipe setShape(List<String> shape){
        recipe.shape(shape.toArray(new String[0]));
        return this;
    }
    public SimpleCraftingRecipe setMaterial(String c, ItemType type){
        if(type instanceof CustomItemType){
            recipe.setIngredient(c.charAt(0), new RecipeChoice.ExactChoice(type.getItemStack()));
        }else{
            recipe.setIngredient(c.charAt(0), type.getMaterial());
        }
        ingredients.put(c, type);
        return this;
    }
    public void register(){
        Bukkit.addRecipe(recipe);
    }
    public ItemStack getProduct(){
        return recipe.getResult();
    }
}
