package cn.jason31416.betternations.structure.types;

import cn.jason31416.betternations.manager.ItemCraftingManager;
import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.nation.TownRole;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.planetlib.ColorUtils;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
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
            new GUISession(player){
                Town curtown = town;
                int memberPage=0;
                @Override
                public void constructGUI(String guiID, GUI gui) {
                    switch (guiID) {
                        case "town-core": {
                            gui.placeholder("town_name", curtown.getName())
                                .placeholder("nation", new StringMessage(curtown.getNation().getColorTag() + curtown.getNation().getName()).toString());
                            if(curtown.getLevel().getNextLevel()!=null) {
                                StringBuilder barbuilder = new StringBuilder("&b");
                                for (int i = 0; i <= 10; i++) {
                                    int x = (int) ((curtown.devPoints-curtown.getLevel().points)/(curtown.getLevel().getNextLevel().points-curtown.getLevel().points)*10);
                                    if (x == i) barbuilder.append("&7");
                                    if(i<x) barbuilder.append("=");
                                    else barbuilder.append("-");
                                }
                                gui.getItems("info")
                                        .setMaterial(curtown.getLevel().material)
                                        .placeholder("hp", ""+curtown.townHealth)
                                        .placeholder("level", new StringMessage(curtown.getLevel().name).toString())
                                        .placeholder("level_bar", new StringMessage(barbuilder.toString()).toString())
                                        .placeholder("dev_points", ""+Math.round(curtown.devPoints*100)/100.0)
                                        .placeholder("next_level_points", ""+curtown.getLevel().getNextLevel().points)
                                        .placeholder("next_level", new StringMessage(curtown.getLevel().getNextLevel().name).toString());
                            }else{
                                gui.getItems("info")
                                        .removeLoreLine(1)
                                        .placeholder("nation", curtown.getNation().getColorTag() + curtown.getNation().getName())
                                        .placeholder("level", curtown.getLevel().name)
                                        .setMaterial(curtown.getLevel().material);
                            }
                            gui.getItems("nation")
                                    .setClickHandler(new GUI.SwitchGuiRunnable("nation-core"));
                            gui.getItems("members")
                                    .setClickHandler((s, a, e) -> {
                                        memberPage = 0;
                                        s.display("town-members");
                                    });
                            break;
                        }
                        case "town-members": {
                            gui.placeholder("town_name", curtown.getName());
                            gui.getItems("back").setClickHandler(new GUI.SwitchGuiRunnable("town-core"));
                            List<SimplePlayer> members = new ArrayList<>();
                            members.add(curtown.getMayor());
                            for(SimplePlayer i: curtown.roles.keySet()) if(curtown.getRole(i) == TownRole.MANAGER) members.add(i);
                            for(SimplePlayer i: curtown.roles.keySet()) if(curtown.getRole(i) == TownRole.RESIDENT) members.add(i);
                            for(SimplePlayer i: curtown.roles.keySet()) if(curtown.getRole(i) == TownRole.GREENCARD) members.add(i);
                            for(SimplePlayer i: curtown.getNation().getMembers()) if(curtown.getRole(i) == TownRole.NONE) members.add(i);

                            int totalpages = members.size()/28+1;
                            if(memberPage < totalpages-1){
                                gui.getItems("nextpage").setMaterial(Material.LIME_STAINED_GLASS_PANE)
                                        .setClickHandler((session, action, event) -> {
                                            memberPage++;
                                            constructGUI(guiID, gui);
                                        });
                            }else gui.getItems("nextpage").setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName(" ");
                            if(memberPage > 0){
                                gui.getItems("prevpage").setMaterial(Material.LIME_STAINED_GLASS_PANE)
                                        .setClickHandler((session, action, event) -> {
                                            memberPage--;
                                            constructGUI(guiID, gui);
                                        });
                            }else gui.getItems("prevpage").setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName(" ");
                            members = members.subList(memberPage*28, Math.min(memberPage*28+28, members.size()));
                            int cur=0;
                            for(int i=10;i<44;i++){
                                if(i%9==8) i+=2;
                                if(cur>=members.size()) break;
                                SimplePlayer p = members.get(cur);
                                ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                                if(head.getItemMeta() instanceof SkullMeta meta){
                                    try {
                                        meta.setOwnerProfile(p.offlinePlayer().getPlayerProfile());
                                    }catch (Exception ignored){}
                                    meta.setDisplayName("§e§l"+p.getName());
                                    meta.setLore(MessageLoader.getList((curtown.getRole(player) == TownRole.MAYOR||player.offlinePlayer().isOp())?"town.gui-member-lore":"town.gui-member-lore-no-permission").add("permission", curtown.getRole(p).getName()).asList());
                                    head.setItemMeta(meta);
                                }
                                gui.addItem("memberitem-"+i, i).setAsVanillaItemStack(head);
                                int pos=i;
                                if(curtown.getRole(player) == TownRole.MAYOR||player.offlinePlayer().isOp()) {
                                    gui.getItems("memberitem-" + i).setClickHandler(new GUI.GUIRunnable() {
                                        private void updateItem(){
                                            ItemStack h = new ItemStack(Material.PLAYER_HEAD);
                                            if(h.getItemMeta() instanceof SkullMeta meta){
                                                try {
                                                    meta.setOwnerProfile(p.offlinePlayer().getPlayerProfile());
                                                }catch(Exception ignored){}
                                                meta.setDisplayName("§e§l"+p.getName());
                                                meta.setLore(MessageLoader.getList(curtown.getRole(player) == TownRole.MAYOR?"town.gui-member-lore":"town.gui-member-lore-no-permission").add("permission", curtown.getRole(p).getName()).asList());
                                                h.setItemMeta(meta);
                                            }
                                            gui.getItem(pos).setAsVanillaItemStack(h);
                                            gui.update();
                                        }
                                        @Override
                                        public void run(GUISession session, InventoryAction action, InventoryClickEvent event) {
                                            if(curtown.getRole(player) != TownRole.MAYOR) return;
                                            if (action == InventoryAction.PICKUP_ALL) {
                                                if (curtown.getRole(p) != TownRole.NONE&&curtown.getRole(p) != TownRole.MAYOR) {
                                                    curtown.setRole(p, curtown.getRole(p).demote());
                                                    updateItem();
                                                }
                                            }else if (action == InventoryAction.PICKUP_HALF) {
                                                if (curtown.getRole(p) != TownRole.MAYOR) {
                                                    curtown.setRole(p, curtown.getRole(p).promote());
                                                    if(curtown.getRole(p)==TownRole.MAYOR){
                                                        curtown.setRole(curtown.getMayor(), TownRole.MAYOR.demote());
                                                        curtown.mayor = p;
                                                        session.display("town-members");
                                                    }else updateItem();
                                                }
                                            }
                                        }
                                    });
                                }
                                cur++;
                            }
                            break;
                        }
                        case "nation-core": {
                            gui.placeholder("nation", curtown.getNation().getName());
                            // todo
                            break;
                        }
                    }
                }
            }.display("town-core");
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
