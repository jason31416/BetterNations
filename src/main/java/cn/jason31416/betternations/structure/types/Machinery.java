package cn.jason31416.betternations.structure.types;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.betternations.manager.ItemCraftingManager;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.betternations.structure.PlaceableStructure;
import cn.jason31416.betternations.structure.upgrade.UpgradeInfo;
import cn.jason31416.betternations.structure.upgrade.UpgradeType;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.item.CustomItemType;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.item.SimpleRecipe;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Machinery extends PlaceableStructure {
    public static Set<Machinery> machineries = new HashSet<>();

    public static Map<String, Map<String, Recipe>> recipes = new HashMap<>();
    public static Map<String, Material> materialMap = new HashMap<>();
    public static class Recipe implements SimpleRecipe {
        public final ItemType product, ingredient;
        public final long duration;
        public final String machineryType;
        public final int productAmount;
        public Recipe(ItemType ingredient, ItemType product, long duration, int productAmount, String machineryType) {
            this.product = product;
            this.duration = duration;
            this.ingredient = ingredient;
            this.machineryType = machineryType;
            this.productAmount = productAmount;
        }
        @Override
        public ItemStack getProduct() {
            return product.getItemStack(productAmount);
        }
    }

    public ItemStack inputSlot=null, outputSlot=null;
    public String currentProducing=null;
    public long finishTime=0;
    public String type;
    public String[] upgrades = new String[]{"", "", "", "", "", "", "", "", ""};

    public String getHologramText(){
        return Message.of("&8-= "+Message.getMessage("structure."+type+".hologram").toFormatted()+" &8=-").toString();
    }

    public void register(){
        super.register();
        machineries.add(this);
    }
    public void unregister(){
        super.unregister();
        machineries.remove(this);
    }

    @Override
    public Material getMaterial() {
        return materialMap.get(type);
    }
    @Override
    public void breakStructure() {
        if(Bukkit.isPrimaryThread()) {
            hologram.removeHologram();
            location.setBlockMaterial(Material.AIR);
        }else{
            new BukkitRunnable() {
                public void run(){
                    hologram.removeHologram();
                    location.setBlockMaterial(Material.AIR);
                }
            }.runTaskLater(BetterNations.instance, 0);
        }
        exists = false;
        if(location.getBukkitLocation().getWorld()==null||!CustomItemType.itemTypes.containsKey(type)) return;
        location.getBukkitLocation().getWorld().dropItem(location.getBukkitLocation(), ItemType.getItemType(type).getItemStack());

        if(inputSlot != null) location.getBukkitLocation().getWorld().dropItem(location.getBukkitLocation(), inputSlot.clone());
        if(outputSlot!= null) location.getBukkitLocation().getWorld().dropItem(location.getBukkitLocation(), outputSlot.clone());

        for (int i = 0; i < 9; i++) {
            if(!upgrades[i].isEmpty())
                location.getBukkitLocation().getWorld().dropItemNaturally(location.getBukkitLocation(), ItemType.getItemType(upgrades[i]).getItemStack());
        }
    }
    @Override
    public boolean serialize(IDataItem dataItem) {
        if (currentProducing != null) dataItem.set("cp", currentProducing);
        dataItem.set("ct", finishTime);
        dataItem.set("tp", type);

        dataItem.set("is", inputSlot);
        dataItem.set("os", outputSlot);
        dataItem.set("upgrades", String.join("/", Arrays.stream(upgrades).map(s -> s.isEmpty() ? "*" : s).toList()));
        return true;
    }
    public void onGUIOpen(GUI gui) {
        flushUpgradeSlot(gui, null);
    }
    @Override
    public void deserialize(IDataItem dataItem) {
        currentProducing = dataItem.getString("cp");
        finishTime = dataItem.getLong("ct");
        type = dataItem.getString("tp");

        inputSlot = (ItemStack) dataItem.get("is");
        outputSlot = (ItemStack) dataItem.get("os");

        upgrades = Arrays.stream(dataItem.getString("upgrades").split("/")).map(s -> s.equals("*") ? "" : s).toList().toArray(new String[9]);
    }
    public String formatTime(long time){
        long hours = time/3600000;
        long minutes = (time%3600000)/60000;
        long seconds = (time%60000)/1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    public String getGUIName(){
        return "machinery-1-1";
    }
    @Override
    public boolean processInteraction(AbstractStructure.InteractionType t, SimplePlayer player) {
        if(t == AbstractStructure.InteractionType.INTERACT){
            if(player.hasPermission(Permission.STRUCTURE, location)||!location.getChunkLocation().isClaimed()){
                new GUISession(player) {
                    public void loadItems(GUI gui){
                        if(currentProducing!=null){
                            gui.getItems("progress-item")
                                    .setMaterial(Material.ARROW)
                                    .setName(Message.getMessage("structure.machinery.progress-item.active.name").toString())
                                    .setLore(MessageLoader.getList("structure.machinery.progress-item.active.lore").add("time", formatTime(finishTime-System.currentTimeMillis())).asList());
                        }else{
                            gui.getItems("progress-item")
                                    .setMaterial(Material.BARRIER)
                                    .setName(Message.getMessage("structure.machinery.progress-item.inactive.name").toString())
                                    .setLore(MessageLoader.getList("structure.machinery.progress-item.inactive.lore").asList());
                        }
//                        System.out.println(inputSlot+" "+outputSlot+" "+currentProducing+" "+finishTime);
                        if(inputSlot!=null){
                            gui.getItems("input-slot").setItemStack(inputSlot);
                        }else{
                            gui.getItems("input-slot")
                                    .setName(Message.getMessage("structure.machinery.input").toString())
                                    .setMaterial(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                                    .setGlow(false)
                                    .setLore(new ArrayList<>())
                                    .setSkullID(null)
                                    .setQuantity(1);
                        }
                        if(outputSlot!=null){
                            gui.getItems("output-slot").setItemStack(outputSlot);
                        }else{
                            gui.getItems("output-slot")
                                    .setName(Message.getMessage("structure.machinery.output").toString())
                                    .setMaterial(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                                    .setGlow(false)
                                    .setLore(new ArrayList<>())
                                    .setSkullID(null)
                                    .setQuantity(1);
                        }
                        onGUIOpen(gui);
                    }
                    @Override
                    public void constructGUI(String guiID, GUI gui) {
                        updateMachinery();
                        gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                        loadItems(gui);
                        gui.getItems("input-slot").setClickHandler((session, a, c)-> {
                            if (a == InventoryAction.PICKUP_ALL && inputSlot != null) {
                                player.getPlayer().setItemOnCursor(inputSlot.clone());
                                inputSlot = null;
                                loadItems(gui);
                                gui.update();
                                if(currentProducing!=null){
                                    currentProducing = null;
                                    finishTime = 0;
                                }
                            }else if(a == InventoryAction.SWAP_WITH_CURSOR && inputSlot == null){
                                inputSlot = player.getPlayer().getItemOnCursor().clone();
                                player.getPlayer().setItemOnCursor(null);
                                loadItems(gui);
                                gui.update();
                            }else if(a == InventoryAction.SWAP_WITH_CURSOR){
                                if(ItemType.getItemType(inputSlot).getName().equals(ItemType.getItemType(player.getPlayer().getItemOnCursor().clone()).getName())){
                                    int transfer = Math.min(inputSlot.getMaxStackSize()-inputSlot.getAmount(), player.getPlayer().getItemOnCursor().getAmount());
                                    inputSlot.setAmount(inputSlot.getAmount()+transfer);
                                    player.getPlayer().getItemOnCursor().setAmount(player.getPlayer().getItemOnCursor().getAmount()-transfer);
                                    loadItems(gui);
                                    gui.update();
                                }
                            }
                        });
                        gui.getItems("output-slot").setClickHandler((session, a, c) -> {
                            if (a == InventoryAction.PICKUP_ALL && outputSlot != null) {
                                player.getPlayer().setItemOnCursor(outputSlot.clone());
                                outputSlot = null;
                                loadItems(gui);
                                gui.update();
                            }
                        });
                        gui.placeholder("structure_name", Message.getMessage("structure."+type+".hologram").toString());

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if(!GUISession.sessions.containsKey(player)||
                                        GUISession.sessions.get(player).gui!=gui){
                                    cancel();
                                    return;
                                }
                                loadItems(gui);
                                gui.update();
                            }
                        }.runTaskTimer(BetterNations.instance, 5L,5L);
                    }
                }.display(getGUIName());
            }else{
                Message.getMessage("town.cannot-build").sendActionbar(player);
            }
        }else if(t == AbstractStructure.InteractionType.BREAK){
            breakStructure();
            unregister();
            return false;
        }
        return true;
    }

    public synchronized void updateMachinery(){
        int speed = 100;
        for (int i = 0; i < 9; i++) {
            if(upgrades[i].isEmpty())
                continue;
            UpgradeInfo info = ItemCraftingManager.upgradeInfoMap.get(upgrades[i]);
            if(info.type() == UpgradeType.SPEED)
                speed += info.value();
        }

        if(currentProducing!=null){
            Recipe recipe = recipes.get(type).get(currentProducing);
            if(recipe == null ||
                    (outputSlot!=null && (outputSlot.getAmount()+recipe.productAmount>outputSlot.getMaxStackSize() || !recipe.product.equals(ItemType.getItemType(outputSlot))))||
                    inputSlot == null ||
                    !ItemType.getItemType(inputSlot).getName().toLowerCase(Locale.ROOT).equals(currentProducing)
            ){
                currentProducing = null;
                finishTime = 0;
                return;
            }
            if(System.currentTimeMillis()>=finishTime){
                finishTime = 0;
                currentProducing = null;
                inputSlot.setAmount(inputSlot.getAmount()-1);
                if(inputSlot.getAmount()<=0){
                    inputSlot = null;
                }
                if(outputSlot == null){
                    outputSlot = recipe.product.getItemStack(recipe.productAmount);
                }else{
                    outputSlot.setAmount(outputSlot.getAmount()+recipe.productAmount);
                }
            }
        }
        if(currentProducing == null && inputSlot != null){
            String inputName = ItemType.getItemType(inputSlot).getName().toLowerCase(Locale.ROOT);
//            System.out.println(inputName+" "+recipes.get(type).containsKey(inputName));
            if(!recipes.get(type).containsKey(inputName)) return;
//            System.out.println("!");
            Recipe recipe = recipes.get(type).get(inputName);
            if(outputSlot!=null && (outputSlot.getAmount()+recipe.productAmount>outputSlot.getMaxStackSize() || !recipe.product.equals(ItemType.getItemType(outputSlot)))) return;
//            System.out.println("!");
            currentProducing = inputName;
            finishTime = System.currentTimeMillis() + (long)(recipe.duration*1000L/(speed/100.0));
            // TODO:动态计算时间
        }
    }

    @NotNull
    public Set<UpgradeType> getUsableUpgrades() {
        return Set.of(UpgradeType.SLOT, UpgradeType.SPEED);
    }

    private void flushUpgradeSlot(@NotNull GUI gui, @Nullable Player argument) {
        int slotLimit = 1;
        for (int i = 0; i < 9; i++) {
            if(upgrades[i].isEmpty())
                continue;
            UpgradeInfo upgradeInfo = ItemCraftingManager.upgradeInfoMap.get(upgrades[i]);
            if(upgradeInfo != null && upgradeInfo.type() == UpgradeType.SLOT) {
                slotLimit += upgradeInfo.value();
            }
        }
        slotLimit = Math.min(slotLimit, 9);
        for (int i = 0; i < 9; i++) {
            boolean usable = (i < slotLimit);
            gui.removeItem(i + 27);
            int fi = i;
            if(upgrades[i].isEmpty()) {
                if(usable) {
                    gui
                            .addItem(UUID.randomUUID().toString(), i + 27, getUpgradeSlotItem(true))
                            .setClickHandler((session, a, c) -> {
                                Player player = session.player.getPlayer();
                                UpgradeInfo upgradeInfo = ItemCraftingManager.upgradeInfoMap.get(ItemType.getItemType(player.getItemOnCursor()).getName());
                                GUI.Item item = session.gui.getItem(fi + 27);
                                if (a == InventoryAction.SWAP_WITH_CURSOR && item != null && upgradeInfo != null && getUsableUpgrades().contains(upgradeInfo.type())) {
                                    upgrades[c.getSlot() - 27] = ItemType.getItemType(player.getItemOnCursor()).getName();
                                    player.setItemOnCursor(null);
                                    flushUpgradeSlot(session.gui, player);
                                }
                            });
                } else {
                    gui.addItem(UUID.randomUUID().toString(), i + 27, getUpgradeSlotItem(false));
                }
            } else {
                if(usable) {
                    gui
                            .addItem(UUID.randomUUID().toString(), i + 27, ItemType.getItemType(upgrades[i]).getItemStack())
                            .setClickHandler((session, a, c) -> {
                                Player player = session.player.getPlayer();
                                GUI.Item item = session.gui.getItem(fi + 27);
                                if (a == InventoryAction.PICKUP_ALL) {
                                    player.setItemOnCursor(ItemType.getItemType(upgrades[fi]).getItemStack());
                                    upgrades[fi] = "";
                                    item.setItemStack(getUpgradeSlotItem(true));
                                    flushUpgradeSlot(gui, player);
                                }
                            });
                } else {
                    returnToPlayer(Objects.requireNonNull(argument), ItemType.getItemType(upgrades[i]).getItemStack());
                    upgrades[i] = "";
                    gui.addItem(UUID.randomUUID().toString(), i + 27, getUpgradeSlotItem(false));
                }
            }
            gui.update();

//            if(!usable) {
//                if(!upgrades[i].isEmpty())
//                    returnToPlayer(Objects.requireNonNull(argument), ItemType.getItemType(upgrades[i]).getItemStack());
//                upgrades[i] = "";
//                if(gui.getItem(i + 27) != null)
//                    gui.removeItem(i + 27);
//                gui.addItem(UUID.randomUUID().toString(), i + 27, getUpgradeSlotItem(false));
//            } else if(upgrades[i].isEmpty()) {
//                if(gui.getItem(i + 27) != null)
//                    gui.removeItem(i + 27);
//                gui.addItem(UUID.randomUUID().toString(), i + 27, getUpgradeSlotItem(true));
//            }
//            GUI.Item item = gui.getItem(i + 27);
//            item.setClickHandler(usable ? (session, a, c) -> {
//                Player player = session.player.getPlayer();
//                GUI.Item slotItem = gui.getItem(c.getSlot());
//                ItemType cursorItem = ItemType.getItemType(player.getItemOnCursor());
//                UpgradeInfo upgradeInfo = ItemCraftingManager.upgradeInfoMap.get(cursorItem.getName());
//                if (a == InventoryAction.SWAP_WITH_CURSOR && slotItem.material == Material.GREEN_STAINED_GLASS_PANE && upgradeInfo != null && usableUpgrades.contains(upgradeInfo.type())) {
//                    slotItem.setItemStack(player.getItemOnCursor());
//                    upgrades[c.getSlot() - 27] = ItemType.getItemType(player.getItemOnCursor()).getName();
//                    player.setItemOnCursor(null);
//                    flushUpgradeSlot(session.gui, player);
//                } else if (a == InventoryAction.PICKUP_ALL && !(slotItem.material == Material.GREEN_STAINED_GLASS_PANE || slotItem.material == Material.BARRIER)) {
//                    player.setItemOnCursor(ItemType.getItemType(upgrades[c.getSlot() - 27]).getItemStack());
//                    upgrades[c.getSlot() - 27] = "";
//                    slotItem.setItemStack(getUpgradeSlotItem(true));
//                    flushUpgradeSlot(gui, player);
//                }
//            } : ((session, a, c) -> {}));
        }
    }

    public static void updateMachineries() {
        for(Machinery machinery: machineries){
            machinery.updateMachinery();
        }
    }

    private void returnToPlayer(@NotNull Player player, @NotNull ItemStack item) {
        if (item.getAmount() <= 0) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        Map<Integer, ItemStack> leftOver = inventory.addItem(item);

        if (!leftOver.isEmpty()) {
            for (ItemStack leftoverItem : leftOver.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
            }
        }
    }

    private @NotNull ItemStack getUpgradeSlotItem(boolean usable) {
        ItemStack item = new ItemStack(usable ? Material.GREEN_STAINED_GLASS_PANE : Material.BARRIER);
        ItemMeta meta = Objects.requireNonNull(item.getItemMeta());
        meta.setDisplayName(Message.getMessage(usable ? "structure.upgrade.usable-slot-name" : "structure.upgrade.unusable-slot-name").toString());
        meta.setLore(MessageLoader.getList(usable ? "structure.upgrade.usable-slot-lore" : "structure.upgrade.unusable-slot-lore").asList());
        item.setItemMeta(meta);
        return item;
    }
}
