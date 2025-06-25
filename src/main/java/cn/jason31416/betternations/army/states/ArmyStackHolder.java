package cn.jason31416.betternations.army.states;

import cn.jason31416.betternations.army.ArmyStack;
import cn.jason31416.planetlib.wrapper.SimpleLocation;

public interface ArmyStackHolder {
    ArmyStack getStack();
    SimpleLocation getLocation();
}
