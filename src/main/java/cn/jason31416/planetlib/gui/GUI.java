package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUI {
    public interface GUIRunnable {
        void run(GUISession session, InventoryAction action, ClickType clickType);
    }
    public static class CommandRunnable implements GUIRunnable {
        public String command;
        public CommandRunnable(String command) {
            this.command = command;
        }
        public void run(GUISession session, InventoryAction action, ClickType clickType) {
            Bukkit.dispatchCommand(session.player.getPlayer(), command);
        }
    }
    public static class SwitchGuiRunnable implements GUIRunnable {
        public String guiName;
        public SwitchGuiRunnable(String guiName) {
            this.guiName = guiName;
        }
        public void run(GUISession session, InventoryAction action, ClickType clickType) {
            session.display(guiName);
        }
    }
    public static class CloseGuiRunnable implements GUIRunnable {
        public void run(GUISession session, InventoryAction action, ClickType clickType) {
            session.close();
        }
    }
    public static class ItemGroup {
        List<Item> items=new ArrayList<>();
        public ItemGroup(List<Item> items){
            this.items=items;
        }
        public ItemGroup setItemStack(ItemStack stack){
            for(Item item: items){
                item.setItemStack(stack);
            }
            return this;
        }
        public ItemGroup setName(String name){
            for(Item item : items){
                item.setName(name);
            }
            return this;
        }
        public ItemGroup setMaterial(Material material){
            for(Item item : items){
                item.setMaterial(material);
            }
            return this;
        }
        public ItemGroup setQuantity(int quantity){
            for(Item item : items){
                item.setQuantity(quantity);
            }
            return this;
        }
        public ItemGroup setLore(List<String> lore){
            for(Item item : items){
                item.setLore(lore);
            }
            return this;
        }
        public ItemGroup setClickHandler(GUIRunnable clickHandler){
            for(Item item : items){
                item.setClickHandler(clickHandler);
            }
            return this;
        }
        public ItemGroup placeholder(String placeholder, String value){
            for(Item item : items){
                item.placeholder(placeholder, value);
            }
            return this;
        }
    }
    @SuppressWarnings("UnusedReturnValue")
    public static class Item {
        public String name="", id;
        public int quantity=1;
        public int slot=0;
        public Material material=Material.AIR;
        public List<String> lore=new ArrayList<>();
        public GUIRunnable clickHandler=null;
        public Item(String id) {this.id = id;}
        public Item setMaterial(Material material) {
            this.material = material;
            return this;
        }
        public Item setItemStack(ItemStack stack) {
            material = stack.getType();
            quantity = stack.getAmount();
            ItemMeta meta = stack.getItemMeta();
            if(meta==null) return this;
            name=meta.getDisplayName();
            lore=meta.getLore();
            return this;
        }
        public Item setName(String name) {
            this.name = name;
            return this;
        }
        public Item setQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }
        public Item setLore(List<String> lore) {
            this.lore = lore;
            return this;
        }
        public Item setSlot(int slot) {
            this.slot = slot;
            return this;
        }
        public Item setClickHandler(GUIRunnable clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }
        public Item placeholder(String placeholder, String value){
            name = name.replace(placeholder, value);
            lore.replaceAll(s -> s.replace(placeholder, value));
            return this;
        }
        public ItemStack toBukkitItem() {
            ItemStack item = new ItemStack(material, quantity);
            ItemMeta meta = item.getItemMeta();
            if(meta!= null){
                meta.setDisplayName(name);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            return item;
        }
        public Item copy() {
            Item item = new Item(id);
            item.name = name;
            item.quantity = quantity;
            item.material = material;
            item.lore = lore;
            item.slot = slot;
            return item;
        }
    }
    public Map<Integer, Item> container=new HashMap<>();
    public int size;
    public String title;
    public Inventory lstInventory;
    public Item getItem(int slot){
        return container.get(slot);
    }
    public ItemGroup getItems(String id){
        List<Item> items = new ArrayList<>();
        for(Item item : container.values()){
            if(item.id.equals(id)){
                items.add(item);
            }
        }
        return new ItemGroup(items);
    }
    public GUI placeholder(String placeholder, String value){
        for(Item item : container.values()){
            item.placeholder("%"+placeholder+"%", value);
        }
        title = title.replace("%"+placeholder+"%", value);
        return this;
    }
    public Item addItem(String id, String name, int slot, Material material, int quantity){
        Item item = new Item(id);
        item.setName(name);
        item.setMaterial(material);
        item.setQuantity(quantity);
        item.setSlot(slot);
        container.put(slot, item);
        return item;
    }
    public List<Item> addItem(String id, String name, List<Integer> slots, Material material, int quantity){
        List<Item> items = new ArrayList<>();
        for(int i : slots) {
            Item item = new Item(id);
            item.setName(name);
            item.setMaterial(material);
            item.setQuantity(quantity);
            item.setSlot(i);
            container.put(i, item);
        }
        return items;
    }
    protected ItemStack putNbt(Item item){
        ItemStack itemStack = item.toBukkitItem();
        NbtHook.setTag(itemStack, "bn.guiItem", item.id);
        return itemStack;
    }
    public void handleClick(int slot, GUISession session, InventoryAction action, ClickType clickType){
        Item item = container.get(slot);
        if(item!= null && item.clickHandler != null){
            item.clickHandler.run(session, action, clickType);
        }
    }
    public void display(SimplePlayer player){
        Inventory inv = Bukkit.createInventory(null, size, new StringMessage(title).toString());
        lstInventory = inv;
        for(Item item : container.values()){
            inv.setItem(item.slot, putNbt(item));
        }
//        Item filleritem = new Item("_filler").setMaterial(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName(" ");
//        for(int i=0;i<inv.getSize();i++){
//            if(inv.getItem(i)==null||inv.getItem(i).getType()==Material.AIR){
//                inv.setItem(i, putNbt(filleritem));
//            }
//        }
        player.getPlayer().openInventory(inv);
    }
    public void update(){
        if(lstInventory != null){
            lstInventory.clear();
            for(Item item : container.values()){
                lstInventory.setItem(item.slot, putNbt(item));
            }
//            Item filleritem = new Item("_filler").setMaterial(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName(" ");
//            for(int i=0;i<lstInventory.getSize();i++){
//                if(lstInventory.getItem(i)==null||lstInventory.getItem(i).getType()==Material.AIR){
//                    lstInventory.setItem(i, putNbt(filleritem));
//                }
//            }
        }
    }
    public GUI copy(){
        GUI gui = new GUI();
        gui.size = size;
        gui.title = title;
        for(int key : container.keySet()){
            gui.container.put(key, container.get(key).copy());
        }
        return gui;
    }
}
