package cn.jason31416.betternations.command.nation;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.NationType;
import cn.jason31416.planetlib.Config;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.message.StaticMessages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
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
        if(context.getPlayer().getLocation().getChunkLocation().isClaimed()){
            return Message.getMessage("command.failed.already-claimed-by-nation");
        }
        String nationName = context.getArg(0);
        if(Nation.getNation(nationName)!=null){
            return Message.getMessage("command.failed.nation-already-exists");
        }
        List<NationType> types = List.of(NationType.values());
        GUISession session = new GUISession(context.getSender().toPlayer()) {
            int curtp = 0;
            NationType nationType = types.get(curtp);
            private void updateNationTypeItem(GUI gui){
                gui.getItems("nation-type")
                        .setMaterial(Material.getMaterial(MessageLoader.instance.getStringMessage("nation.type."+nationType.name().toLowerCase()+".material", "AIR").toString()))
                        .setName(Message.getMessage("nation.type.gui-name").add("type_name", Message.getMessage("nation.type."+nationType.name().toLowerCase()+".name").toString()).toString())
                        .setLore(MessageLoader.getList("nation.type.gui-lore").add("type_description", Message.getMessage("nation.type."+nationType.name().toLowerCase()+".description").toString()).asList());
                gui.update();
            }
            @Override
            public void constructGUI(String guiID, GUI gui) {
                if (guiID.equals("create-nation")) {
                    gui.placeholder("nation_name", nationName);
                    gui.getItems("nation-type").setClickHandler((session, action, clickType) -> {
                        curtp=(curtp+1)%types.size();
                        nationType = types.get(curtp);
                        updateNationTypeItem(gui);
                    });
                    updateNationTypeItem(gui);
                    gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());
                    gui.getItems("confirm").setClickHandler((session, action, clickType) -> {
                        session.close();
                        if (context.getPlayer().getNation() != null) {
                            return;
                        }
                        if(Nation.getNation(nationName)!=null){
                            context.getSender().sendMessage(Message.getMessage("command.failed.nation-already-exists"));
                            return;
                        }
                        if(!player.withdrawBalance(Config.getDouble("nation.creation-cost"))){
                            context.getSender().sendMessage(Message.getMessage("command.failed.not-enough-money").add("amount", Config.getDouble("nation.creation-cost")));
                            return;
                        }
                        if(player.getLocation().getBlockMaterial().isSolid()||player.getLocation().getChunkLocation().isClaimed()){
                            context.getSender().sendMessage(Message.getMessage("command.failed.invalid-creation-location"));
                            return;
                        }
                        Nation nation = Nation.createNation(context.getPlayer(), nationName);
                        if(nation==null){
                            context.getSender().sendMessage(Message.getMessage("command.failed.failed-create-nation").add("name", nationName));
                            return;
                        }
                        nation.setType(nationType);
                        context.getSender().sendMessage(Message.getMessage("command.success.nation-created").add("name", nationName));
                    });
                }
            }
        };
        session.display("create-nation");
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return List.of("<name>");
    }
}
