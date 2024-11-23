package cn.jason31416.betternations.army.states;

import cn.jason31416.betternations.army.ArmyStack;

public abstract class ArmyStackHolder {
    public ArmyStack stack=null;
    public void mergeIn(ArmyStack stack1){
        if(stack == null){
            stack = stack1;
        }else{
            stack.addArmyStack(stack1);
        }
    }
    public abstract void update();
}
