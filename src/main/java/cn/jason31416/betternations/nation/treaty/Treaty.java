package cn.jason31416.betternations.nation.treaty;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import java.util.*;

public class Treaty {
    public enum TreatyState {
        EDITING,
        PROPOSED,
        ACTIVE
    }
    public static Map<String, Treaty> treatyMap=new HashMap<>();
    public String name;
    public SimplePlayer proposer;
    public UUID id;
    public List<Term> terms=new ArrayList<>();
    public Set<Nation> signed=new HashSet<>();
    public TreatyState state = TreatyState.EDITING;
    public Treaty(String name, SimplePlayer proposer){
        this.name = name;
        this.proposer = proposer;
        this.id = UUID.randomUUID();
        treatyMap.put(name, this);
    }
    public void display(SimplePlayer player){
        MessageLoader.getList("treaty.display.header").add("treaty", name).send(player);
        int cnt=1;
        for(Term i: terms){
            if(state==TreatyState.EDITING&&proposer.equals(player)) Message.getMessage("treaty.display.editing-term").add("id", cnt++).add("content", i.getDescription()).add("treaty", name).send(player);
            else if(!i.check(this)) Message.getMessage("treaty.display.cannot-execute-term").add("id", cnt++).add("content", i.getDescription()).send(player);
            else if(i.from==player.getNation()||i.target==player.getNation()) Message.getMessage("treaty.display.affected-term").add("id", cnt++).add("content", i.getDescription()).send(player);
            else Message.getMessage("treaty.display.normal-term").add("id", cnt++).add("content", i.getDescription()).send(player);
        }
        new StringMessage("").send(player);
        if(state==TreatyState.PROPOSED) Message.getMessage("treaty.display.status-pending").send(player);
        else if(state==TreatyState.EDITING) Message.getMessage("treaty.display.status-editing").send(player);
        else if(state==TreatyState.ACTIVE) Message.getMessage("treaty.display.status-active").send(player);
        if(state==TreatyState.PROPOSED){
            Set<Nation> affected = getAffected();
            MessageLoader.getList("treaty.display.footer-proposed")
                    .add("proposer", proposer.getName())
                    .add("signed", String.join(", ", signed.stream().map(Nation::getName).toList()))
                    .add("waiting", String.join(", ", affected.stream().filter(n->!signed.contains(n)).map(Nation::getName).toList()))
                    .send(player);
            if(affected.contains(player.getNation())){
                new StringMessage("").send(player);
                String s="";
                if(signed.contains(player.getNation())) s += Message.getMessage("treaty.display.signed-button").toFormatted();
                else s += Message.getMessage("treaty.display.sign-button").add("treaty", name).toFormatted();
                s += Message.getMessage("treaty.display.veto-button").add("treaty", name).toFormatted();
                player.sendMessage(new StringMessage(s));
            }
        }else if(state==TreatyState.EDITING&&(proposer.equals(player)||player.getPlayer().isOp())){
            new StringMessage("").send(player);
            Message.getMessage("treaty.display.add-term-button").add("treaty", name).send(player);
            Message.getMessage("treaty.display.submit-button").add("treaty", name).send(player);
            Message.getMessage("treaty.display.delete-button").add("treaty", name).send(player);
        }
    }
    public Set<Nation> getAffected(){
        Set<Nation> ret = new HashSet<>();
        for(Term i: terms){
            ret.add(i.from);
            ret.add(i.target);
        }
        return ret;
    }
    public void submit(){
        state=TreatyState.PROPOSED;
        Message.getMessage("treaty.submitted").add("id", id.toString()).add("proposer", proposer.getName()).add("treaty", name).broadcast();
    }
    public int sign(Nation nation){
        if(!getAffected().contains(nation)||signed.contains(nation)||state!=TreatyState.PROPOSED) return 0;
        signed.add(nation);
        Message.getMessage("treaty.signed").add("nation", nation.getName()).add("id", id.toString()).add("treaty", name).broadcast();
        if(getAffected().size()==signed.size()){
            for(Term i: terms){
                if(!i.check(this)){
                    signed.remove(nation);
                    return -1;
                }
            }
            for(Term i: terms) i.execute();
            state=TreatyState.ACTIVE;
            Message.getMessage("treaty.activated").add("id", id.toString()).add("treaty", name).broadcast();
        }
        return 1;
    }
    public boolean veto(Nation nation){
        if(!getAffected().contains(nation)||state!=TreatyState.PROPOSED) return false;
        signed.clear();
        state = TreatyState.EDITING;
        Message.getMessage("treaty.vetoed").add("nation", nation.getName()).add("id", id.toString()).add("name", name).broadcast();
        return true;
    }
}
