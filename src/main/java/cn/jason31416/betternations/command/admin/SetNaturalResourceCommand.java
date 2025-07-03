package cn.jason31416.betternations.command.admin;


import cn.jason31416.betternations.manager.NaturalResourcesManager;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.planetlib.item.CustomItemType;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.message.Message;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetNaturalResourceCommand extends ChildCommand {

    public SetNaturalResourceCommand(IParentCommand parent) {
        super("resource", parent);
    }

    @Override
    public @Nullable Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        if(!context.getPlayer().getPlayer().isOp()) return Message.getMessage("command.failed.no-permission");
        ItemType itemType=null;
        if(!context.args().isEmpty()){
            try {
                itemType = ItemType.getItemType(context.getArg(0));
            }catch (Exception e){
                return Message.getMessage("command.failed.invalid-item-type");
            }
        }
        if(itemType==null){
            NaturalResourcesManager.naturalResourcesMap.remove(context.getPlayer().getLocation().getChunkLocation());
        }else{
            NaturalResourcesManager.naturalResourcesMap.put(context.getPlayer().getLocation().getChunkLocation(), itemType);
        }
        return Message.getMessage("command.success.set-natural-resource").add("resource", context.getArg(0));
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(!context.getPlayer().getPlayer().isOp()) return null;
        List<String> suggestions = new ArrayList<>(Arrays.stream(Material.values()).map(Material::name).map(String::toLowerCase).toList());
        suggestions.addAll(CustomItemType.itemTypes.keySet());
        return suggestions.stream().filter(s -> s.startsWith(context.getArg(0).toLowerCase())).toList();
    }
}
