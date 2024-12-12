package cn.jason31416.betternations.structure;

import cn.jason31416.betternations.army.states.InvasionFlag;
import cn.jason31416.betternations.army.states.StructuredArmy;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.structure.types.Outpost;
import cn.jason31416.betternations.structure.types.UnitProductionStructure;
import cn.jason31416.planetlib.item.CustomItemType;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

public class StructureListener implements Listener {
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event){
        SimpleChunkLocation chunk = SimpleChunkLocation.of(event.getChunk());
        if(Hologram.holograms.containsKey(chunk)) for (Hologram i: Hologram.holograms.get(chunk)){
            i.spawn();
        }
    }
    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event){
        SimpleChunkLocation chunk = SimpleChunkLocation.of(event.getChunk());
        if(Hologram.holograms.containsKey(chunk)) for (Hologram i: Hologram.holograms.get(chunk)){
            i.despawn();
        }
    }
    @EventHandler
    public void onStructureBreak(BlockBreakEvent event) {
        AbstractStructure structure = AbstractStructure.structures.get(SimpleLocation.of(event.getBlock()));
        if (structure!= null) {
            event.setCancelled(true);
            if(structure.processInteraction(AbstractStructure.InteractionType.BREAK, SimplePlayer.of(event.getPlayer()))){
                structure.breakStructure();
                structure.unregister();
            }
        }
    }
    @EventHandler
    public void onStructureInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        AbstractStructure structure = AbstractStructure.structures.get(SimpleLocation.of(event.getClickedBlock()));
        if (structure!=null&&event.getAction()==Action.LEFT_CLICK_BLOCK&&event.getPlayer().isSneaking()){
            SimplePlayer player = SimplePlayer.of(event.getPlayer());
            event.setCancelled(true);
            if((structure instanceof StructuredArmy)||player.hasPermission(Permission.STRUCTURE, SimpleLocation.of(event.getClickedBlock()))){
                structure.processInteraction(AbstractStructure.InteractionType.SNEAK_CLICK, SimplePlayer.of(event.getPlayer()));
            }
        }else if(structure!= null&&!event.getPlayer().isSneaking()){
            if(event.getAction()== Action.RIGHT_CLICK_BLOCK) {
                SimplePlayer player = SimplePlayer.of(event.getPlayer());
                event.setCancelled(true);
                if((structure instanceof StructuredArmy)||player.hasPermission(Permission.STRUCTURE, SimpleLocation.of(event.getClickedBlock()))){
                    structure.processInteraction(AbstractStructure.InteractionType.INTERACT, SimplePlayer.of(event.getPlayer()));
                }
            }
        }else if(event.getAction()== Action.RIGHT_CLICK_BLOCK) {
            SimpleLocation loc = SimpleLocation.of(event.getClickedBlock().getRelative(event.getBlockFace()));
            SimplePlayer player = SimplePlayer.of(event.getPlayer());
            if(!player.hasPermission(Permission.STRUCTURE, SimpleLocation.of(event.getClickedBlock()))){
                event.setCancelled(true);
                return;
            }
            if(loc.getBlockMaterial().isAir()){
                ItemStack hand = event.getItem();
                if(hand==null) return;
                Class<?> clazz = PlaceableStructure.placeableStructures.get(ItemType.getItemType(hand).getName().toLowerCase());
                if(clazz != null){
                    event.setCancelled(true);
                    try {
                        PlaceableStructure ps = (PlaceableStructure) clazz.getDeclaredConstructor().newInstance();
                        ps.location = loc.getBlockLocation();
                        if(ps instanceof UnitProductionStructure ups){
                            ups.type = ItemType.getItemType(hand).getName().toLowerCase();
                        }
                        if((ps instanceof Outpost o)&&Outpost.outposts.contains(o.location.getChunkLocation())){
                            player.sendMessage(Message.getMessage("structure.outpost.already-exists"));
                            return;
                        }
                        ps.place();
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                             InvocationTargetException e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to create structure instance!");
                    }
                    hand.setAmount(hand.getAmount()-1);
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
            if (StructuredArmy.isInvasionChunk(SimpleLocation.of(block).getChunkLocation())||AbstractStructure.structures.containsKey(SimpleLocation.of(block))) {
                event.setCancelled(true);
                return;
            }
        }
    }
    @EventHandler
    public void onStructureRetracted(@Nonnull BlockPistonRetractEvent event){
        for(Block block : event.getBlocks()) {
            if (StructuredArmy.isInvasionChunk(SimpleLocation.of(block).getChunkLocation())||AbstractStructure.structures.containsKey(SimpleLocation.of(block))) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
