package cn.jason31416.planetlib.item;

import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.message.StaticMessages;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class ItemType {
    public abstract Material getMaterial();
    public abstract String getName();
    public ItemStack getItemStack(){
        return getItemStack(1);
    }
    public ItemStack getItemStack(int amount){
        return new ItemStack(getMaterial(), amount);
    }
    public boolean allowInteraction(){
        return true;
    }
    public static ItemType getItemType(String name){
        if(CustomItemType.itemTypes.containsKey(name.toLowerCase())){
            return CustomItemType.itemTypes.get(name.toLowerCase());
        }
        try{
            return new VanillaItemType(Material.getMaterial(name.toUpperCase()));
        }catch (Exception e){
            throw new RuntimeException("Unable to find the item type: "+name);
        }
    }
    public static ItemType getItemType(ItemStack item){
        if(item == null) return null;
        if(NbtHook.hasTag(item, "plib.itemType")){
            return CustomItemType.get(item);
        }
        return new VanillaItemType(item.getType());
    }
}
