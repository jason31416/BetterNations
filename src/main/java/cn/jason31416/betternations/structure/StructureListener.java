package cn.jason31416.betternations.structure;

import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nonnull;

public class StructureListener implements Listener {
    @EventHandler
    public void onStructureBreak(BlockBreakEvent event) {
        AbstractStructure structure = AbstractStructure.structures.get(SimpleLocation.of(event.getBlock()));
        if (structure!= null) {
            if(!structure.processInteraction(AbstractStructure.InteractionType.BREAK, SimplePlayer.of(event.getPlayer()))){
                event.setCancelled(true);
            }else{
                structure.breakStructure();
            }
        }
    }
    @EventHandler
    public void onStructureInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        AbstractStructure structure = AbstractStructure.structures.get(SimpleLocation.of(event.getClickedBlock()));
        if(structure!= null){
            if(event.getAction()== Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                structure.processInteraction(AbstractStructure.InteractionType.INTERACT, SimplePlayer.of(event.getPlayer()));
            }
        }
    }
    @EventHandler
    public void onWaterFlowStructure(@Nonnull BlockFromToEvent event){
        AbstractStructure structure = AbstractStructure.structures.get(SimpleLocation.of(event.getBlock()));
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
