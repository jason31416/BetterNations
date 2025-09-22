package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.manager.NaturalResourcesManager;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.betternations.item.ItemType;
import cn.jason31416.betternations.item.VanillaItemType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class NationMapCommand extends ChildCommand {
    public NationMapCommand(IParentCommand parent) {
        super("map", parent);
    }
    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.getSender().isPlayer()) return null;
        SimpleChunkLocation center = context.getPlayer().getLocation().getChunkLocation();
        Message.getMessage("map.header").send(context.getSender());
        for (int z=center.z()-Config.getInt("map.height", 3);z<center.z()+Config.getInt("map.height", 3);z++){
            StringBuilder msg = new StringBuilder();
            for(int x=center.x()-Config.getInt("map.width",4);x<center.x()+Config.getInt("map.width",4);x++){
                SimpleChunkLocation chunk = SimpleChunkLocation.of(x, z);
                String addmsg="";
                if(chunk.equals(center)){
                    float direction = context.getPlayer().getPlayer().getLocation().getYaw();
                    if((-180 <= direction && direction <= -135) || (135 < direction && direction <= 180)) {
                        addmsg = MessageLoader.instance.getRawMessage("map.self-chars.up", "").replace("%player%", context.getPlayer().getName());
                    } else if(-135 < direction && direction <= -45) {
                        addmsg = MessageLoader.instance.getRawMessage("map.self-chars.right", "").replace("%player%", context.getPlayer().getName());
                    } else if(-45 < direction && direction <= 45) {
                        addmsg = MessageLoader.instance.getRawMessage("map.self-chars.down", "").replace("%player%", context.getPlayer().getName());
                    } else if(45 <= direction && direction < 135) {
                        addmsg = MessageLoader.instance.getRawMessage("map.self-chars.left", "").replace("%player%", context.getPlayer().getName());
                    }
                } else if(!chunk.isClaimed())
                    addmsg = MessageLoader.instance.getRawMessage("map.wild-char", "");
                else if(chunk.isTownChunk())
                    addmsg = MessageLoader.instance.getRawMessage("map.town-char", "")
                            .replace("%kingdom_color%", Objects.requireNonNull(chunk.getNation()).getColorTag())
                            .replace("%nation_name%", Objects.requireNonNull(chunk.getNation()).getColorTag()+chunk.getNation().getName())
                            .replace("%town_name%", Objects.requireNonNull(chunk.getTown()).getName());
                else
                    addmsg = MessageLoader.instance.getRawMessage("map.nation-char", "")
                            .replace("%kingdom_color%", Objects.requireNonNull(chunk.getNation()).getColorTag())
                            .replace("%nation_name%", Objects.requireNonNull(chunk.getNation()).getColorTag()+chunk.getNation().getName());
                if(!NaturalResourcesManager.naturalResourcesMap.containsKey(chunk)) addmsg = addmsg.replace("%natural_resource%", MessageLoader.instance.getRawMessage("map.natural-resource.none", ""));
                else{
                    ItemType type = NaturalResourcesManager.naturalResourcesMap.get(chunk);
                    String itemName;
                    if(type instanceof VanillaItemType){
                        itemName = "<lang:"+type.getMaterial().getItemTranslationKey()+">";
                    }else{
                        itemName = type.getItemStack().getItemMeta().getDisplayName();
                    }
                    addmsg = addmsg.replace("%natural_resource%", MessageLoader.instance.getRawMessage("map.natural-resource.contains", "")
                            .replace("%resource%", itemName)
                    );
                }
                msg.append(addmsg);
            }
            new StringMessage(msg.toString()).send(context.getSender());
        }
        Message.getMessage("map.footer").send(context.getSender());
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
