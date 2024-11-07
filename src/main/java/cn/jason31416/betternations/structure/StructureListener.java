package cn.jason31416.betternations.structure;

import cn.jason31416.planetlib.item.CustomItemType;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

public class StructureListener implements Listener {
    @EventHandler
    public void onStructureBreak(BlockBreakEvent event) {
        AbstractStructure structure = AbstractStructure.structures.get(SimpleLocation.of(event.getBlock()));
        if (structure!= null) {
            if(structure instanceof PlaceableStructure||structure.processInteraction(AbstractStructure.InteractionType.BREAK, SimplePlayer.of(event.getPlayer()))){
                structure.breakStructure();
                event.setCancelled(true);
            }else{
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onStructureInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        AbstractStructure structure = AbstractStructure.structures.get(SimpleLocation.of(event.getClickedBlock()));
        if(structure!= null&&!event.getPlayer().isSneaking()){
            if(event.getAction()== Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                structure.processInteraction(AbstractStructure.InteractionType.INTERACT, SimplePlayer.of(event.getPlayer()));
            }
        }else if(event.getAction()== Action.RIGHT_CLICK_BLOCK) {
            SimpleLocation loc = SimpleLocation.of(event.getClickedBlock().getRelative(event.getBlockFace()));
            if(loc.getBlockMaterial().isAir()){
                ItemStack hand = event.getItem();
                if(hand==null) return;
                Class<?> clazz = PlaceableStructure.placeableStructures.get(ItemType.getItemType(hand).getName().toLowerCase());
                if(clazz != null){
                    event.setCancelled(true);
                    hand.setAmount(hand.getAmount()-1);
                    try {
                        PlaceableStructure ps = (PlaceableStructure) clazz.getDeclaredConstructor(SimpleLocation.class).newInstance(loc.getBlockLocation());
                        ps.place();
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                             InvocationTargetException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to create structure instance!");
                    }
                }
            }
        }
        if(event.getItem()!=null&&!ItemType.getItemType(event.getItem()).allowInteraction()&&event.getAction()==Action.RIGHT_CLICK_BLOCK){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onGrindstone(PrepareGrindstoneEvent event){
        if((ItemType.getItemType(event.getInventory().getItem(0)) instanceof CustomItemType) || (ItemType.getItemType(event.getInventory().getItem(1)) instanceof CustomItemType)){
            event.setResult(null);
        }
    }
    @EventHandler
    public void onEnchant(PrepareItemEnchantEvent event){
        if(ItemType.getItemType(event.getItem()) instanceof CustomItemType){
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onRename(PrepareAnvilEvent event){
        if((ItemType.getItemType(event.getInventory().getItem(0)) instanceof CustomItemType) || (ItemType.getItemType(event.getInventory().getItem(1)) instanceof CustomItemType)){
            event.setResult(null);
        }
    }
    @EventHandler
    public void onWaterFlowStructure(@Nonnull BlockFromToEvent event){
        AbstractStructure structure = AbstractStructure.structures.get(SimpleLocation.of(event.getToBlock()));
        if(structure != null) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onStructurePushed(@Nonnull BlockPistonExtendEvent event){
        for(Block block : event.getBlocks()) {
            AbstractStructure structure = AbstractStructure.structures.get(SimpleLocation.of(block));
            if (structure != null) {
                event.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler
    public void onStructureRetracted(@Nonnull BlockPistonRetractEvent event){
        for(Block block : event.getBlocks()) {
            AbstractStructure structure = AbstractStructure.structures.get(SimpleLocation.of(block));
            if (structure != null) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
