package cn.jason31416.betternations.command.admin;

import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParameterType;
import cn.jason31416.betternations.item.CustomItemType;
import cn.jason31416.betternations.item.ItemType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GiveCommand extends ChildCommand {
    public GiveCommand(IParentCommand parent) {
        super(List.of("give", "item"), parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(!context.getSender().sender().isOp()) return Message.getMessage("command.failed.no-permission");
        if(!context.checkArgs(ParameterType.STRING)) return null;
        SimplePlayer target=null;
        if(context.getSender().isPlayer()) target=context.getSender().toPlayer();
        if(context.args().size()>=2) target=context.getPlayerArg(1);
        if(target==null||!target.isOnline()) return Message.getMessage("command.failed.invalid-target");
        ItemType type;
        if(CustomItemType.itemTypes.containsKey(context.getArg(0).toLowerCase())){
            type = CustomItemType.itemTypes.get(context.getArg(0).toLowerCase());
        }else return Message.getMessage("command.failed.unknown-item-type");
        target.getPlayer().getInventory().addItem(type.getItemStack());
        return Message.getMessage("command.success.give-item").add("item", type.getName()).add("player", target.getName());
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if(context.getCurrentArg()==1) return new ArrayList<>(CustomItemType.itemTypes.keySet());
        if(context.getCurrentArg()==2) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        return null;
    }
}
