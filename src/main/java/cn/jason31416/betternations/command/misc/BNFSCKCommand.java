package cn.jason31416.betternations.command.misc;

import cn.jason31416.betternations.manager.ItemCraftingManager;
import cn.jason31416.betternations.structure.Hologram;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.item.*;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BNFSCKCommand extends ChildCommand {
    public BNFSCKCommand(IParentCommand parent) {
        super("fsck", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        return Message.getMessage("command.success.checked-holograms");
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
