package cn.jason31416.betternations.structure;

import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public abstract class PlaceableStructure extends AbstractStructure {
    public PlaceableStructure(Material material, SimpleLocation location) {
        super(material, location);
    }

    public static String getType(ItemStack item){
        if(item == null||item.getType() == Material.AIR||item.getAmount() == 0) return null;
        return NbtHook.getTag(item, "bn.structureItem.type");
    }
    public ItemStack setType(ItemStack item){
        if(item == null || item.getType() == Material.AIR || item.getAmount() == 0) return null;
        NbtHook.setTag(item, "bn.structureItem.type", getClass().getSimpleName());
        return item;
    }
    public abstract ItemStack getItem();
    public ItemStack getItemStack(){
        return setType(getItem());
    }
}
