package cn.jason31416.planetlib.item;

import cn.jason31416.planetlib.PlanetLib;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.List;
import java.util.UUID;

public class SimpleFurnaceRecipe implements SimpleRecipe {
    public final FurnaceRecipe recipe;
    public SimpleFurnaceRecipe(ItemStack result, ItemType ingredient, float exp, int cookingTime){
        if(ingredient instanceof CustomItemType){
            recipe = new FurnaceRecipe(new NamespacedKey(PlanetLib.instance, UUID.randomUUID().toString()),
                    result,
                    new RecipeChoice.ExactChoice(ingredient.getItemStack()),
                    exp,cookingTime);
        }else{
            recipe = new FurnaceRecipe(new NamespacedKey(PlanetLib.instance, UUID.randomUUID().toString()),
                    result,
                    ingredient.getMaterial(),
                    exp,cookingTime);
        }
    }
    public void register(){
        Bukkit.addRecipe(recipe);
    }

    @Override
    public ItemStack getProduct() {
        return recipe.getResult();
    }
}
