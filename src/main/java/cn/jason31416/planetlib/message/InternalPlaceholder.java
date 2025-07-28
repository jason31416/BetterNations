package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.hook.PAPIHook;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import me.clip.placeholderapi.PlaceholderAPI;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InternalPlaceholder {
    public static List<PlaceholderHandler> placeholderHandlers = new ArrayList<>();
    public static void registerPlaceholderHandler(PlaceholderHandler handler){
        placeholderHandlers.add(handler);
    }
    public static String replacePlaceholders(String message, @Nullable SimplePlayer player){
        for(PlaceholderHandler handler : placeholderHandlers){
            message = handler.replacePlaceholders(message, player);
        }
        if(PAPIHook.enabled) {
            message = PlaceholderAPI.setPlaceholders(player==null?null:player.offlinePlayer(), message);
        }
        return message;
    }
}
