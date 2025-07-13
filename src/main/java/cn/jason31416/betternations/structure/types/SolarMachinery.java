package cn.jason31416.betternations.structure.types;

import cn.jason31416.betternations.structure.upgrade.UpgradeType;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.gui.GUI;
import org.bukkit.Material;
import org.bukkit.World;

public class SolarMachinery extends Machinery {
    static {
        usableUpgrades.add(UpgradeType.SPEED);
        usableUpgrades.add(UpgradeType.SOLAR);
    }
    @Override
    public boolean serialize(IDataItem dataItem) {
        return super.serialize(dataItem);
    }
    @Override
    public void deserialize(IDataItem dataItem) {
        super.deserialize(dataItem);
    }
    @Override
    public String getGUIName(){
        return "machinery-1-1-solar";
    }
    @Override
    public void onGUIOpen(GUI gui) {
        super.onGUIOpen(gui);
        gui
                .getItems("solar-state")
                .setMaterial(hasSunshine() ? Material.MAGMA_CREAM : Material.SLIME_BALL);
    }
    @Override
    public synchronized void updateMachinery(){
        super.updateMachinery();
        if(currentProducing!=null) {
            if (!hasSunshine()) {
                currentProducing = null;
                finishTime = 0L;
            }
        }
    }

    private boolean hasSunshine() {
        return location.world().getBukkitWorld().getEnvironment() == World.Environment.NORMAL &&
                location.world().getBukkitWorld().isClearWeather() &&
                (location.world().getBukkitWorld().getTime() % 24000 + 24000) % 24000 < 12000;
    }
}
