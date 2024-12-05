package cn.jason31416.betternations.structure.types;

import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;

import java.util.UUID;

public class TownCore extends AbstractStructure {
    public Town town;
    public TownCore() {
    }
    public String getHologramText(){
        return Message.getMessage("structure.towncore.hologram").add("nation_color", town.getNation().getColorTag()).add("town", town.getName()).toString();
    }
    @Override
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public boolean processInteraction(InteractionType type, SimplePlayer player){
        if(type == InteractionType.INTERACT){
            GUISession session = new GUISession(player){
                @Override
                public void constructGUI(String guiID, GUI gui) {
                    switch (guiID) {
                        case "town-core": {
                            gui.placeholder("town_name", town.getName());
                        }
                    }
                }
            };
            session.display("town-core");
        }else if(type == InteractionType.BREAK) {
            return false;
        }
        return true;
    }

    @Override
    public Material getMaterial() {
        return Material.BEACON;
    }

    @Override
    public boolean serialize(IDataItem dataItem) {
        dataItem.set("town", town.getId().toString());
        return true;
    }

    @Override
    public void deserialize(IDataItem dataItem) {
        town = Town.getTown(UUID.fromString(dataItem.getString("town")));
        town.core = this;
    }
}
