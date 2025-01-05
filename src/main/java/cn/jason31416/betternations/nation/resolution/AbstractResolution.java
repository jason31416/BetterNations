package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.betternations.nation.NationType;
import cn.jason31416.betternations.nation.NationalRank;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractResolution {
    public Set<SimplePlayer> requiredSigners, signedPlayers=new HashSet<>();
    public Nation nation;
    public SimplePlayer proposer;
    public long deadline;
    public String resolutionId;
    public boolean isExecuted=false;
    public double requiredRatio=0.5; // 50% required by default
    public int minimalSigners=1; // 1 by default
    public AbstractResolution(Nation nation, SimplePlayer proposer) {
        this.requiredSigners=new HashSet<>();
        if(proposer.getNation() == nation) this.requiredSigners.add(proposer);
        this.nation=nation;
        this.proposer=proposer;
        deadline=System.currentTimeMillis()+1000L*60*60*24*3; // 3 days
        resolutionId=UUID.randomUUID().toString();
    }
    public void propose(){
        if(nation.getType().decisionMaker.makeDecision(this)) {
            for(AbstractResolution res: nation.resolutions.values()){
                if(res.equals(this)){
                    proposer.sendMessage(Message.getMessage("nation.resolution.duplicate-exists")
                            .add("resolution_id", res.resolutionId)
                            .add("resolution_name", res.getResolutionContent().toFormatted()));
                    return;
                }
            }
            nation.resolutions.put(resolutionId, this);
            Message.getMessage("nation.resolution.proposed")
                    .add("proposer", proposer.getName())
                    .add("resolution_id", resolutionId)
                    .add("resolution_name", getResolutionContent().toFormatted())
                    .send(nation.getMembers());
            if(!(this instanceof ImportantResolution)&&!(this instanceof OutsiderResolution)) sign(proposer);
        }else{
            proposer.sendMessage(Message.getMessage("nation.resolution.cannot-propose"));
        }
    }
    public boolean checkDate(){
        return System.currentTimeMillis()<=deadline;
    }
    public boolean canSign(SimplePlayer player){
        return !isExecuted&&checkDate()&&requiredSigners.contains(player)&&!signedPlayers.contains(player);
    }
    public boolean canUnsign(SimplePlayer player){
        return !isExecuted&&checkDate()&&signedPlayers.contains(player);
    }
    public void sign(SimplePlayer player){
        if(!checkDate()) return;
        if(requiredSigners.contains(player)&&!signedPlayers.contains(player)&&!isExecuted){
            signedPlayers.add(player);
            Message.getMessage("nation.resolution.signed")
                    .add("signer", player.getName())
                    .add("resolution_id", resolutionId)
                    .add("resolution_name", getResolutionContent().toFormatted()).send(nation.getMembers());
            if(checkPass()) {
                Message.getMessage("nation.resolution.passed")
                        .add("resolution_id", resolutionId)
                        .add("resolution_name", getResolutionContent().toFormatted()).send(nation.getMembers());
                execute();
                isExecuted = true;
                cancel();
            }
        }
    }
    public void unsign(SimplePlayer player){
        if(!checkDate()) return;
        if(!isExecuted&&signedPlayers.contains(player)) {
            Message.getMessage("nation.resolution.unsigned")
                    .add("signer", player.getName())
                    .add("resolution_id", resolutionId)
                    .add("resolution_name", getResolutionContent().toFormatted()).send(nation.getMembers());
            signedPlayers.remove(player);
        }
    }
    public boolean equals(AbstractResolution resolution){
        return getResolutionContent().toString().equals(resolution.getResolutionContent().toString());
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
    public void addRequiredSigners(List<SimplePlayer> requiredSigners){
        this.requiredSigners.addAll(requiredSigners);
    }
    public void setRequiredRatio(double requiredRatio){
        this.requiredRatio=requiredRatio;
    }
    public void setMinimalSigners(int minimalSigners){
        this.minimalSigners=minimalSigners;
    }
    public boolean checkPass(){
        return !isExecuted&&((signedPlayers.size()>=requiredSigners.size()*requiredRatio&&signedPlayers.size()>=Math.min(minimalSigners, requiredSigners.size()))||(signedPlayers.contains(nation.getOwner())&&nation.getType()==NationType.MONARCHY)); // Over half of the required signers have signed/Monarch signed
    }
    public abstract void execute();
    @Nonnull
    public abstract Message getResolutionContent();
}
