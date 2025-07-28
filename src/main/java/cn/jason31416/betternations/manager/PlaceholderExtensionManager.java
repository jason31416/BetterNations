package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PlaceholderExtensionManager extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "nation";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", BetterNations.instance.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return BetterNations.instance.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer ofpl, @NotNull String params) {
        if(ofpl == null){
            return null;
        }
        SimplePlayer player = SimplePlayer.of(ofpl);
        if(params.equals("name")){
            return player.getNation()==null? Message.getMessage("placeholder.none", "None").toString() :player.getNation().getName();
        }else if(params.equals("leader")){
            return player.getNation()==null? Message.getMessage("placeholder.none", "None").toString() :player.getNation().getOwner().getName();
        }else if(params.equals("members_count")){
            return player.getNation()==null? "0" : String.valueOf(player.getNation().getMembers().size());
        }else if(params.equals("members_list")){
            return player.getNation()==null? Message.getMessage("placeholder.none", "None").toString() : String.join(", ", player.getNation().getMembers().stream().map(SimplePlayer::getName).toList());
        }else if(params.equals("color_code")){
            return player.getNation()==null? "" : player.getNation().getColorTag();
        }else if(params.equals("player_location_nation")){
            return player.getLocation().getChunkLocation().getNation()==null? Message.getMessage("placeholder.none", "None").toString() : player.getLocation().getChunkLocation().getNation().getName();
        }else if(params.equals("player_location_town")){
            return player.getLocation().getChunkLocation().getTown()==null? Message.getMessage("placeholder.none", "None").toString() : player.getLocation().getChunkLocation().getTown().getName();
        }else if(params.equals("player_location_town_nation")){
            if(player.getLocation().getChunkLocation().getNation()==null) return Message.getMessage("placeholder.wilderness", "Wilderness").toString();
            if(player.getLocation().getChunkLocation().getTown()==null){
                return player.getLocation().getChunkLocation().getNation().getName();
            }
            return player.getLocation().getChunkLocation().getTown().getName() + ", " + player.getLocation().getChunkLocation().getNation().getName();
        }else if(params.equals("player_location_color_code")){
            return player.getLocation().getChunkLocation().getNation()==null?"":player.getLocation().getChunkLocation().getNation().getColorTag();
        }
        return null;
    }
}
