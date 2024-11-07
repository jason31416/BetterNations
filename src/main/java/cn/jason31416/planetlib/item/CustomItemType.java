package cn.jason31416.planetlib.item;

import cn.jason31416.planetlib.hook.NbtHook;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class CustomItemType extends ItemType {
    public static Map<String, CustomItemType> itemTypes = new java.util.HashMap<>();
    public final String registryName, displayName;
    public int customModelData=0;
    public final Material material;
    public final List<String> lore;
    public boolean allowInteraction=true;
    public Material getMaterial(){return material;}
    public CustomItemType(String registryName, String displayName, Material material, List<String> lore) {
        this.registryName = registryName;
        this.displayName = displayName;
        this.lore = lore;
        this.material = material;
    }
    public CustomItemType(String registryName, String displayName, Material material, List<String> lore, boolean allowInteraction) {
        this(registryName, displayName, material, lore);
        this.allowInteraction = allowInteraction;
    }
    public ItemStack newItemStack(int amount){
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) throw new RuntimeException("item meta is null");
        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(lore);
        if(customModelData != 0) itemMeta.setCustomModelData(customModelData);
        itemStack.setItemMeta(itemMeta);
        NbtHook.setTag(itemStack, "plib.itemType", registryName);
        return itemStack;
    }
    public boolean allowInteraction(){
        return allowInteraction;
    }
    public CustomItemType setCustomModelData(int customModelData){
        this.customModelData = customModelData;
        return this;
    }
    public void register(){
        itemTypes.put(registryName, this);
    }
    public String getName(){
        return registryName;
    }
    public boolean equals(Object other){
        if(other instanceof CustomItemType type){
            return type.registryName.equals(registryName);
        }
        return false;
    }
    public ItemStack getItemStack(int amount){
        return newItemStack(amount);
    }
    public static CustomItemType get(String registryName){
        return itemTypes.get(registryName);
    }
    public static CustomItemType get(ItemStack itemStack){
        if(NbtHook.hasTag(itemStack, "plib.itemType")){
            return get(NbtHook.getTag(itemStack, "plib.itemType"));
        }
        return null;
    }
}
