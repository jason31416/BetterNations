package cn.jason31416.betternations.structure.types;

import cn.jason31416.betternations.manager.ItemCraftingManager;
import cn.jason31416.betternations.structure.upgrade.UpgradeInfo;
import cn.jason31416.betternations.structure.upgrade.UpgradeType;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.item.ItemType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FueledMachinery extends Machinery {
    public static Map<String, Integer> fuelPointMap=new HashMap<>();
    public static List<String> guiFuelLore = null;
    public static Map<String, Integer> fuelCapacityMap=new HashMap<>();
    public int fuel=0;
    public long lastCheckFuel=0L;

    @Override
    public boolean serialize(IDataItem dataItem) {
        dataItem.set("fuel", fuel);
        return super.serialize(dataItem);
    }
    @Override
    public void deserialize(IDataItem dataItem) {
        fuel = dataItem.getInteger("fuel");
        super.deserialize(dataItem);
    }
    @Override
    public String getGUIName(){
        return "machinery-1-1-fueled";
    }
    @Override
    public void onGUIOpen(GUI gui){
        super.onGUIOpen(gui);
        if(guiFuelLore==null&&!gui.getItems("fuel-slot").items.isEmpty()){
            guiFuelLore = gui.getItems("fuel-slot").items.get(0).lore;
        }
        gui.getItems("fuel-slot")
                .setLore(new ArrayList<>(guiFuelLore))
                .placeholder("fuel_amount", ""+fuel)
                .placeholder("fuel_capacity", ""+fuelCapacityMap.get(type))
                .setClickHandler((session, action, event) -> {
                    Player player = session.player.getPlayer();
                    if(action == InventoryAction.SWAP_WITH_CURSOR && fuel < fuelCapacityMap.get(type)) {
                        String itemType = ItemType.getItemType(player.getItemOnCursor()).getName().toLowerCase(Locale.ROOT);
                        if (fuelPointMap.containsKey(itemType)) {
                            int count = player.getItemOnCursor().getAmount();
                            while (count > 0 && fuel < fuelCapacityMap.getOrDefault(type, 100)) {
                                fuel = Math.min(fuel + fuelPointMap.get(itemType), fuelCapacityMap.getOrDefault(type, 100));
                                count--;
                            }
                            player.getItemOnCursor().setAmount(count);
                            gui.update();
                        }
                    }
                });
        if(fuel==0) gui.getItems("fuel-slot").setMaterial(Material.BUCKET);
        else gui.getItems("fuel-slot").setMaterial(Material.LAVA_BUCKET);
    }
    @Override
    public synchronized void updateMachinery(){
        super.updateMachinery();
        if(currentProducing!=null) {
            if (lastCheckFuel != 0) {
                if (System.currentTimeMillis()/1000 > lastCheckFuel) {
                    fuel = Math.max(0, fuel - (int) (System.currentTimeMillis()/1000 - lastCheckFuel));
                }
            }
            if (fuel == 0) {
                currentProducing = null;
                finishTime = 0L;
            }
        }
        lastCheckFuel = System.currentTimeMillis()/1000;
    }

    @Override
    @NotNull
    public Set<UpgradeType> getUsableUpgrades() {
        return Set.of(UpgradeType.SLOT, UpgradeType.EFFICIENCY, UpgradeType.SPEED);
    }
}
