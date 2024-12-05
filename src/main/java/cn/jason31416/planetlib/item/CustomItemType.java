package cn.jason31416.planetlib.item;

import cn.jason31416.planetlib.InvalidConfigurationException;
import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.message.StaticMessages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomItemType extends ItemType {
    public static Map<String, CustomItemType> itemTypes = new java.util.HashMap<>();
    public final String registryName, displayName;
    public int customModelData=0;
    public final Material material;
    public final String skullValue;
    public final List<String> lore;
    public boolean glow=false;
    public boolean allowInteraction=true;
    public Material getMaterial(){return material;}
    public CustomItemType(String registryName, String displayName, Material material, List<String> lore) {
        this.registryName = registryName;
        this.displayName = displayName;
        this.lore = lore;
        this.material = material;
        this.skullValue = null;
    }
    public CustomItemType(String registryName, String displayName, String skullValue, List<String> lore) {
        this.registryName = registryName;
        this.displayName = displayName;
        this.lore = lore;
        this.material = Material.PLAYER_HEAD;
        this.skullValue = skullValue;
        this.allowInteraction = false;
    }
    public CustomItemType(String registryName, String displayName, Material material, List<String> lore, boolean allowInteraction) {
        this(registryName, displayName, material, lore);
        this.allowInteraction = allowInteraction;
    }
    public ItemStack newItemStack(int amount) {
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if(itemMeta == null) throw new RuntimeException("item meta is null");
        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(lore);
        if(customModelData != 0) itemMeta.setCustomModelData(customModelData);
        try {
            if(itemMeta instanceof SkullMeta meta&&skullValue!=null){
                PlayerProfile profile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());
                PlayerTextures textures = profile.getTextures();
                textures.setSkin(new URL("https://textures.minecraft.net/texture/"+skullValue));
                profile.setTextures(textures);
                meta.setOwnerProfile(profile);
            }
        } catch (MalformedURLException ignored) {
            throw new RuntimeException(ignored);
        }
        if(glow){
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
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
