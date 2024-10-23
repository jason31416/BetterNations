package cn.jason31416.betternations.manager;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import org.bukkit.event.block.*;

import cn.jason31416.planetlib.wrapper.*;
import cn.jason31416.planetlib.Config;

public class EventListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
    }
}
