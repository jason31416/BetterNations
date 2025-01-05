package cn.jason31416.betternations.nation.treaty;

import cn.jason31416.betternations.nation.Nation;

public abstract class Term {
    public Nation from, target;
    public Term(Nation from, Nation target){
        this.from = from;
        this.target = target;
    }
    public boolean check(Treaty treaty){
        return true;
    }
    public abstract void execute();
    public abstract String getDescription();
}
