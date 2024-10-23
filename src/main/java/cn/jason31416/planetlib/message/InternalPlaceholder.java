package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.wrapper.SimplePlayer;

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
        return message;
    }
}
