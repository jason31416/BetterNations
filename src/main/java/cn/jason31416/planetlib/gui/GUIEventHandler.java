package cn.jason31416.planetlib.gui;


import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class GUIEventHandler implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack itemStack = event.getCurrentItem();
        if (!NbtHook.hasTag(itemStack, "bn.guiItem")) {
            return;
        }
        if(event.getClickedInventory() == null) return;
        event.setCancelled(true);
        SimplePlayer player = SimplePlayer.of((Player) event.getWhoClicked());
        if(GUISession.sessions.containsKey(player)&&event.getClickedInventory().equals(GUISession.sessions.get(player).getCurrentInventory())){
            GUISession session = GUISession.sessions.get(player);
            session.handleClick(event.getSlot(), event.getClick(), event.getAction());
        }else{
            event.getClickedInventory().remove(event.getCurrentItem());
            StaticMessages.UNKNOWN_GUI_ITEM.sendConsole();
        }
    }
}
