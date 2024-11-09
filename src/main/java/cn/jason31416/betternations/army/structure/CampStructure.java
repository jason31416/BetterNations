package cn.jason31416.betternations.army.structure;


import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.betternations.nation.Permission;
import cn.jason31416.betternations.structure.AbstractStructure;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;

public class CampStructure extends UnitStructure { // The block state of the army
    public CampStructure() {
        super(Material.PLAYER_HEAD);
    }
    public CampStructure(SimpleLocation location, ArmyStack stack) {
        super(Material.PLAYER_HEAD, location);
        this.stack = stack;
    }

    @Override
    public boolean processInteraction(InteractionType type, SimplePlayer player) {
        if(type == InteractionType.BREAK){
            if(stack.isEntityForm) throw new RuntimeException("Army stack is not block form!");
            if(!player.hasPermission(Permission.MANAGE_ARMY)) return false;
            stack.convertToEntity(player);
            return true;
        }
        return false;
    }
}