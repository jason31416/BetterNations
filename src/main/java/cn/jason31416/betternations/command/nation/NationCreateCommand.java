package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.message.Message;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static cn.jason31416.planetlib.command.ParameterType.*;

public class NationCreateCommand extends ChildCommand {
    public NationCreateCommand(IParentCommand parent) {
        super("create", parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null||!context.checkArgs(STRING)) return null;
        if(context.getPlayer().getNation()!=null){
            return Message.getMessage("command.failed.player-already-in-nation");
        }
        String nationName = context.getArg(0);
        if(Nation.getNation(nationName)!=null){
            return Message.getMessage("command.failed.nation-already-exists");
        }
        if(GUISession.sessions.containsKey(context.getPlayer())){
            return Message.getMessage("command.failed.player-in-gui");
        }
        GUISession session = new GUISession(context.getSender().toPlayer()) {
            @Override
            public void constructGUI(String guiID, GUI gui) {
                if (guiID.equals("create-nation")) {
                    gui.placeholder("nation_name", nationName);
                    gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                    gui.getItems("confirm").setClickHandler((session, action, clickType) -> {
                        if (context.getPlayer().getNation() != null) {
                            return;
                        }
                        Nation.createNation(context.getPlayer(), nationName);
                        context.getSender().sendMessage(Message.getMessage("command.success.nation-created").add("nation", nationName));
                    });
                }
            }
        };
        session.display("create-nation");
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
