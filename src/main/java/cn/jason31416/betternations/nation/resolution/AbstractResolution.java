package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.NationalRank;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractResolution {
    public Set<SimplePlayer> requiredSigners, signedPlayers=new HashSet<>();
    public Nation nation;
    public SimplePlayer proposer;
    public long deadline;
    public UUID resolutionId;
    public boolean isExecuted=false;
    public double requiredRatio=0.5; // 50% required by default
    public int minimalSigners=1; // 1 by default
    public AbstractResolution(Nation nation, SimplePlayer proposer) {
        this.requiredSigners=new HashSet<>();
        this.nation=nation;
        this.proposer=proposer;
        deadline=System.currentTimeMillis()+1000L*60*60*24*3; // 3 days
        resolutionId=UUID.randomUUID();
    }
    public void propose(){
        if(nation.getType().decisionMaker.makeDecision(this)) {
            nation.resolutions.put(resolutionId, this);
            // todo: send interaction message to all members of the nation
        }
    }
    public void sign(SimplePlayer player){
        if(requiredSigners.contains(player)&&!isExecuted){
            signedPlayers.add(player);
            if(checkPass()) {
                execute();
                isExecuted = true;
                cancel();
            }
        }
    }
    public void unsign(SimplePlayer player){
        if(!isExecuted) signedPlayers.remove(player);
    }
    public void cancel(){
        nation.resolutions.remove(resolutionId);
    }
    public void setRequiredSigners(List<NationalRank> requiredSigners){
        for(SimplePlayer player: nation.getMembers()){
            if(requiredSigners.contains(player.getRank())){
                this.requiredSigners.add(player);
            }
        }
    }
    public void setRequiredRatio(double requiredRatio){
        this.requiredRatio=requiredRatio;
    }
    public void setMinimalSigners(int minimalSigners){
        this.minimalSigners=minimalSigners;
    }
    public boolean checkPass(){
        return signedPlayers.size()>=requiredSigners.size()*requiredRatio&&(requiredSigners.size()<minimalSigners||signedPlayers.size()>=minimalSigners); // Over half of the required signers have signed
    }
    public abstract void execute();
    public abstract String getResolutionContent();
}
