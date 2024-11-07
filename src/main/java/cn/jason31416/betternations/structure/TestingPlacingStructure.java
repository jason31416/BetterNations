package cn.jason31416.betternations.structure;

import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.item.ItemType;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Material;

public class TestingPlacingStructure extends PlaceableStructure {
    public TestingPlacingStructure(){
        super(Material.TORCH);
    }
    public TestingPlacingStructure(SimpleLocation location) {
        super(Material.TORCH, ItemType.getItemType("testingitem"), location);
    }

    @Override
    public boolean serialize(IDataItem dataItem) {
        return true;
    }

    @Override
    public void deserialize(IDataItem dataItem) {
    }

    @Override
    public boolean processInteraction(InteractionType type, SimplePlayer player) {
        player.sendMessage(new StringMessage("Hello there, I am a torch XD"));
        return false;
    }
}
