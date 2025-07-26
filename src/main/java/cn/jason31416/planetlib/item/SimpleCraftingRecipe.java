package cn.jason31416.planetlib.item;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.planetlib.InvalidConfigurationException;
import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.message.StaticMessages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SimpleCraftingRecipe implements SimpleRecipe {
    public static class RecipeMatrix {
        public ItemType[][] recipe=new ItemType[3][3];

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof RecipeMatrix that)) return false;
            if(that.recipe.length!=recipe.length||that.recipe[0].length!=recipe[0].length) return false;
            for(int i=0;i<recipe.length;i++){
                if(that.recipe[i].length!=recipe[i].length) return false;
                for(int j=0;j<recipe[i].length;j++){
                    if(!Objects.equals(that.recipe[i][j], recipe[i][j])) return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            StringBuilder s = new StringBuilder();
            for (ItemType[] itemTypes : recipe) {
                for (ItemType itemType : itemTypes) {
                    if(itemType==null) return 0;
                    s.append(itemType.getName()).append("|");
                }
            }
            return s.toString().hashCode();
        }
    }
    public static class recipeListener implements Listener {
        @EventHandler
        public void preCrafting(PrepareItemCraftEvent event) {
            ItemStack[] matrix = event.getInventory().getMatrix();
            boolean hasNonnull=false, hasCustom=false;
            for(ItemStack itemStack: matrix){
                if(itemStack!=null) hasNonnull = true;
                if(NbtHook.hasTag(itemStack, "plib.itemType")&&CustomItemType.itemTypes.containsKey(NbtHook.getTag(itemStack, "plib.itemType"))){
                    hasCustom = true;
                }
            }
            if(!hasNonnull) return;
            if(matrix.length==9){
                RecipeMatrix rm = new RecipeMatrix();
                for(int i=0;i<9;i++){
                    if(matrix[i]==null) rm.recipe[i/3][i%3] = null;
                    else rm.recipe[i/3][i%3] = ItemType.getItemType(matrix[i]);
                }
                if(recipes.containsKey(rm)){
                    SimpleCraftingRecipe r = recipes.get(rm);
                    event.getInventory().setResult(r.result.getItemStack(r.rescount));
                }else if(hasCustom){
                    event.getInventory().setResult(null);
                }
            }else if(hasCustom){
                event.getInventory().setResult(null);
            }
        }
    }
    public static Map<RecipeMatrix, SimpleCraftingRecipe> recipes=new HashMap<>();
    public RecipeMatrix recipeMatrix;
    public final ItemType result;
    public final int rescount;
    public List<String> shape = null;
    public Map<String, ItemType> ingredients = new HashMap<>();
    public SimpleCraftingRecipe(ItemType result, int rescount){
        this.result = result;
        this.rescount = rescount;
    }
    public SimpleCraftingRecipe setShape(List<String> shape){
        this.shape = shape;
        return this;
    }
    public SimpleCraftingRecipe setMaterial(String c, ItemType type){
        ingredients.put(c, type);
        return this;
    }
    public void register(@Nullable String uuid){
        ingredients.put(" ", null);

        RecipeMatrix rm = new RecipeMatrix();
        if(shape.size()!=3||shape.get(0).length()!=3||shape.get(1).length()!=3||shape.get(2).length()!=3){
            throw new RuntimeException("Invalid shape for recipe: "+uuid);
        }
        for(int i=0;i<shape.size();i++){
            for(int j=0;j<shape.get(i).length();j++){
                String c = shape.get(i).substring(j,j+1);
                if(ingredients.containsKey(c)){
                    rm.recipe[i][j] = ingredients.get(c);
                }else{
                    throw new RuntimeException("Undefined ingredient for recipe: "+uuid+", item '"+c+"'");
                }
            }
        }
        if(recipes.containsKey(rm)){
            throw new RuntimeException("Duplicate shape for recipe: "+uuid+"!");
        }
        recipeMatrix = rm;
        recipes.put(rm, this);
    }
    public ItemStack getProduct(){
        return result.getItemStack(rescount);
    }
}
