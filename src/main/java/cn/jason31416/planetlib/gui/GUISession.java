package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public abstract class GUISession {
    public static final Map<SimplePlayer, GUISession> sessions = new java.util.HashMap<>();
    public final SimplePlayer player;
    private GUI gui;
    public GUISession(SimplePlayer player) {
        this.player = player;
        sessions.put(player, this);
    }
    public abstract void constructGUI(String guiID, GUI gui);
    public void display(GUI gui) {
        if(gui == null) throw new IllegalArgumentException("GUI cannot be null");
        this.gui = gui;
        gui.display(player);
    }
    public void handleClick(int slot, ClickType clickType, InventoryAction action) {
        gui.handleClick(slot, this, action, clickType);
    }
    public void display(String guiID){
        try {
            GUI gui = GUILoader.getGUI(guiID);
            constructGUI(guiID, gui);
            display(gui);
        } catch (Exception e) {
            player.sendMessage(new StringMessage("&cError: Missing GUI "+guiID+", please contact admin!"));
            PlanetLib.instance.getLogger().severe("Missing GUI: "+gui+"!");
        }
    }
    public void close() {
        sessions.remove(player);
        player.getPlayer().closeInventory();
    }
    public Inventory getCurrentInventory() {
        return gui.lstInventory;
    }
}
