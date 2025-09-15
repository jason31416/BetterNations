package cn.jason31416.betternations.structure;

import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.betternations.army.ArmyType;
import cn.jason31416.betternations.army.states.ArmyCamp;
import cn.jason31416.betternations.army.states.StructuredArmy;
import cn.jason31416.betternations.manager.LandArmyManager;
import cn.jason31416.betternations.manager.NaturalResourcesManager;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.structure.types.Extractor;
import cn.jason31416.betternations.structure.types.Machinery;
import cn.jason31416.betternations.structure.types.Outpost;
import cn.jason31416.betternations.structure.types.UnitProductionStructure;
import cn.jason31416.planetlib.item.CustomItemType;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import java.util.HashSet;
import java.util.Set;

public class StructureListener implements Listener {
    static Set<Material> interactable = new HashSet<>();
    static {
        interactable.add(Material.DARK_OAK_DOOR);
        interactable.add(Material.ACACIA_DOOR);
        interactable.add(Material.BIRCH_DOOR);
        interactable.add(Material.JUNGLE_DOOR);
        interactable.add(Material.SPRUCE_DOOR);
        interactable.add(Material.OAK_DOOR);
        interactable.add(Material.ACACIA_FENCE_GATE);
        interactable.add(Material.BIRCH_FENCE_GATE);
        interactable.add(Material.DARK_OAK_FENCE_GATE);
        interactable.add(Material.JUNGLE_FENCE_GATE);
        interactable.add(Material.SPRUCE_FENCE_GATE);
        interactable.add(Material.OAK_FENCE_GATE);
        interactable.add(Material.ACACIA_TRAPDOOR);
        interactable.add(Material.BIRCH_TRAPDOOR);
        interactable.add(Material.DARK_OAK_TRAPDOOR);
        interactable.add(Material.JUNGLE_TRAPDOOR);
        interactable.add(Material.SPRUCE_TRAPDOOR);
        interactable.add(Material.OAK_TRAPDOOR);
        interactable.add(Material.CRAFTING_TABLE);
        interactable.add(Material.FURNACE);
        interactable.add(Material.BLAST_FURNACE);
        interactable.add(Material.SMOKER);
        interactable.add(Material.CARTOGRAPHY_TABLE);
        interactable.add(Material.GRINDSTONE);
        interactable.add(Material.ANVIL);
        interactable.add(Material.CHIPPED_ANVIL);
        interactable.add(Material.DAMAGED_ANVIL);
        interactable.add(Material.BREWING_STAND);
        interactable.add(Material.CHEST);
        interactable.add(Material.TRAPPED_CHEST);
        interactable.add(Material.ENDER_CHEST);
        interactable.add(Material.HOPPER);
        interactable.add(Material.DISPENSER);
        interactable.add(Material.DROPPER);
    }
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
        if (event.getClickedBlock() != null) {
            AbstractStructure structure = AbstractStructure.structures.get(SimpleLocation.of(event.getClickedBlock()));
            if (structure != null && event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().isSneaking()) {
                SimplePlayer player = SimplePlayer.of(event.getPlayer());
                event.setCancelled(true);
                if ((structure instanceof StructuredArmy) || player.hasPermission(Permission.STRUCTURE, SimpleLocation.of(event.getClickedBlock()))) {
                    structure.processInteraction(AbstractStructure.InteractionType.SNEAK_CLICK, SimplePlayer.of(event.getPlayer()));
                }
            } else if (structure != null && !event.getPlayer().isSneaking()) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    SimplePlayer player = SimplePlayer.of(event.getPlayer());
                    event.setCancelled(true);
                    if ((structure instanceof StructuredArmy) || player.hasPermission(Permission.STRUCTURE, SimpleLocation.of(event.getClickedBlock()))) {
                        structure.processInteraction(AbstractStructure.InteractionType.INTERACT, SimplePlayer.of(event.getPlayer()));
                    }
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                SimpleLocation loc = SimpleLocation.of(event.getClickedBlock().getRelative(event.getBlockFace()));
                SimplePlayer player = SimplePlayer.of(event.getPlayer());
                if (!player.hasPermission(Permission.STRUCTURE, SimpleLocation.of(event.getClickedBlock()))) {
                    event.setCancelled(true);
                    return;
                }
                if (loc.getBlockMaterial().isAir()) {
                    ItemStack hand = event.getItem();
                    if (hand == null) return;
                    Class<?> clazz = PlaceableStructure.placeableStructures.get(ItemType.getItemType(hand).getName().toLowerCase());
                    if (clazz != null) {
                        event.setCancelled(true);
                        try {
                            PlaceableStructure ps = (PlaceableStructure) clazz.getDeclaredConstructor().newInstance();
                            ps.location = loc.getBlockLocation();
                            if (ps instanceof UnitProductionStructure ups) {
                                ups.type = ItemType.getItemType(hand).getName().toLowerCase();
                            }
                            if (ps instanceof Machinery m) {
                                m.type = ItemType.getItemType(hand).getName().toLowerCase();
                            }
                            if ((ps instanceof Outpost o) && Outpost.outposts.contains(o.location.getChunkLocation())) {
                                player.sendMessage(Message.getMessage("structure.outpost.already-exists"));
                                return;
                            }
                            if ((ps instanceof Extractor e)) {
                                if (Extractor.extractors.containsKey(e.location.getChunkLocation())) {
                                    player.sendMessage(Message.getMessage("structure.extractor.already-exists"));
                                    return;
                                }
                                if (!NaturalResourcesManager.naturalResourcesMap.containsKey(e.location.getChunkLocation())) {
                                    player.sendMessage(Message.getMessage("structure.extractor.no-resources"));
                                    return;
                                }
                            }
                            ps.place();
                        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                                 InvocationTargetException e) {
                            e.printStackTrace();
                            throw new RuntimeException("Failed to create structure instance!");
                        }
                        hand.setAmount(hand.getAmount() - 1);
                    }else if(LandArmyManager.directPlacements.containsKey(ItemType.getItemType(hand).getName())) {
                        event.setCancelled(true);
                        SimpleChunkLocation chunk = loc.getChunkLocation();
                        SimpleLocation clicked = SimpleLocation.of(event.getClickedBlock());
                        ArmyType type = LandArmyManager.directPlacements.get(ItemType.getItemType(hand).getName());
                        if (AbstractStructure.structures.containsKey(clicked) && AbstractStructure.structures.get(clicked) instanceof ArmyCamp camp && camp.stack.nation==player.getNation() &&
                                clicked.getChunkLocation().isTownChunk()&&clicked.getChunkLocation().getNation()==player.getNation()){
                            camp.stack.addArmy(type, 1);
                            camp.stack.supply += type.maxSupply;
                            camp.updateHologram();
                            hand.setAmount(hand.getAmount() - 1);
                        }else if(chunk.isTownChunk()&&chunk.getNation()==player.getNation()){
                            ArmyStack stack = new ArmyStack(player.getNation());
                            stack.addArmy(type, 1);
                            stack.supply = type.maxSupply;
                            ArmyCamp camp = new ArmyCamp();
                            camp.location = loc.getBlockLocation();
                            camp.stack = stack;
                            camp.place();
                            hand.setAmount(hand.getAmount() - 1);
                        }
                    }
                }
            }
        }
        if(event.getItem()!=null&&!ItemType.getItemType(event.getItem()).allowInteraction()&&(event.getAction()==Action.RIGHT_CLICK_BLOCK||event.getAction()==Action.RIGHT_CLICK_AIR)&&(event.getPlayer().isSneaking()||event.getClickedBlock()==null||!interactable.contains(event.getClickedBlock().getType()))){
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
