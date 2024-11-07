package cn.jason31416.betternations.command.misc;

import cn.jason31416.betternations.manager.ItemCraftingManager;
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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingGuideCommand extends ChildCommand {
    public CraftingGuideCommand(IParentCommand parent) {
        super(List.of("recipe", "guide"), parent);
    }

    @Nullable
    @Override
    public Message execute(ICommandContext context) {
        if(context.getPlayer()==null) return null;
        SimplePlayer player = context.getPlayer();
        GUISession session = new GUISession(player) {
            int page=0, recipePage=0;
            CustomItemType selectedType=null;
            @Override
            public void constructGUI(String guiID, GUI gui) {
                switch (guiID){
                    case "custom-items": {
                        int totalpages = CustomItemType.itemTypes.size()/28+1;
                        if(page < totalpages-1){
                            gui.getItems("nextpage").setMaterial(Material.LIME_STAINED_GLASS_PANE)
                                    .setClickHandler((session, action, clickType) -> {
                                page++;
                                constructGUI(guiID, gui);
                            });
                        }else gui.getItems("nextpage").setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName(" ");
                        if(page > 0){
                            gui.getItems("prevpage").setMaterial(Material.LIME_STAINED_GLASS_PANE)
                                    .setClickHandler((session, action, clickType) -> {
                                page--;
                                constructGUI(guiID, gui);
                            });
                        }else gui.getItems("prevpage").setMaterial(Material.BLACK_STAINED_GLASS_PANE).setName(" ");
                        gui.getItems("close").setClickHandler(new GUI.CloseGuiRunnable());

                        List<CustomItemType> types = new ArrayList<>(CustomItemType.itemTypes.values()).subList(page*28, Math.min(CustomItemType.itemTypes.size(), page*28+28));
                        int cur=0;
                        for(int i=10;i<44;i++){
                            if(i%9==8) i+=2;
                            if(cur>=types.size()) break;
                            CustomItemType t = types.get(cur);
                            gui.addItem("item-"+i, t.displayName, i, t.material, 1)
                                    .setLore(t.lore);
                            if(ItemCraftingManager.recipes.get(types.get(cur))!=null)
                                gui.getItems("item-"+i).setClickHandler((session, action, clickType) -> {
                                        selectedType = t;
                                        recipePage = 0;
                                        session.display("recipe-display");
                                    });
                            cur++;
                        }
                    }
                    case "recipe-display": {
                        if(selectedType==null) return;
                        List<SimpleRecipe> recipes = ItemCraftingManager.recipes.get(selectedType);
                        if(recipes==null||recipePage>=recipes.size()) return;
                        gui.placeholder("item", new StringMessage(selectedType.displayName).toFormatted());
                        if(recipePage<recipes.size()-1) gui.getItems("nextpage").setClickHandler((session, b, c)->{
                            recipePage++;
                            session.display("recipe-display");
                        });
                        else gui.getItems("nextpage").setMaterial(Material.BLACK_STAINED_GLASS_PANE);
                        if(recipePage>0) gui.getItems("prevpage").setClickHandler((session, b, c)->{
                            recipePage--;
                            session.display("recipe-display");
                        });
                        else gui.getItems("prevpage").setMaterial(Material.BLACK_STAINED_GLASS_PANE);
                        gui.getItems("product").setItemStack(recipes.get(recipePage).getProduct());
                        gui.getItems("back").setClickHandler((session, b, c) -> session.display("custom-items"));
                        if(recipes.get(recipePage) instanceof SimpleCraftingRecipe recipe){
                            gui.getItems("process")
                                    .setName(Message.getMessage("item.recipe.crafting.name").toString())
                                    .setMaterial(Material.CRAFTING_TABLE)
                                    .setLore(MessageLoader.getList("item.recipe.crafting.lore").asList());
                            String[] shape = recipe.recipe.getShape();
                            for(int i=0;i<shape.length;i++){
                                for(int j=0;j<shape[i].length();j++){
                                    if(!recipe.ingredients.containsKey(shape[i].substring(j, j + 1))){
                                        gui.getItems("slot"+(i+1)+"-"+(j+1)).setMaterial(Material.AIR);
                                        continue;
                                    }
                                    gui.getItems("slot"+(i+1)+"-"+(j+1))
                                            .setItemStack(recipe.ingredients.get(shape[i].substring(j, j+1)).getItemStack());
                                    if(recipe.ingredients.get(shape[i].substring(j, j+1)) instanceof CustomItemType rc){
                                        gui.getItems("slot"+(i+1)+"-"+(j+1))
                                                .setClickHandler((session, action, clicktype) -> {
                                                    selectedType = rc;
                                                    recipePage = 0;
                                                    session.display("recipe-display");
                                                });
                                    }
                                }
                            }
                        }else if(recipes.get(recipePage) instanceof SimpleFurnaceRecipe recipe){
                            gui.getItems("process")
                                    .setName(Message.getMessage("item.recipe.smelting.name").toString())
                                    .setMaterial(Material.FURNACE)
                                    .setLore(MessageLoader.getList("item.recipe.smelting.lore")
                                            .add("smelting-time", recipe.recipe.getCookingTime()/20.0)
                                            .asList());
                            gui.getItems("slot2-2").setItemStack(recipe.recipe.getInput());
                            gui.getItems("slot1-1").setMaterial(Material.WHITE_STAINED_GLASS_PANE);
                            gui.getItems("slot1-2").setMaterial(Material.WHITE_STAINED_GLASS_PANE);
                            gui.getItems("slot1-3").setMaterial(Material.WHITE_STAINED_GLASS_PANE);
                            gui.getItems("slot2-1").setMaterial(Material.WHITE_STAINED_GLASS_PANE);
                            gui.getItems("slot2-3").setMaterial(Material.WHITE_STAINED_GLASS_PANE);
                            gui.getItems("slot3-1").setMaterial(Material.WHITE_STAINED_GLASS_PANE);
                            gui.getItems("slot3-2").setMaterial(Material.WHITE_STAINED_GLASS_PANE);
                            gui.getItems("slot3-3").setMaterial(Material.WHITE_STAINED_GLASS_PANE);
                        }
                    }
                }
            }
        };
        session.display("custom-items");
        return null;
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return null;
    }
}
