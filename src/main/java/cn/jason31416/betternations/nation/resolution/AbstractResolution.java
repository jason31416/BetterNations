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
    public AbstractResolution(Nation nation, SimplePlayer proposer) {
        requiredSigners = new HashSet<>();
        for(SimplePlayer member: nation.getMembers()){
            if(member.getRank().weight()>=500) requiredSigners.add(member);
        }
        this.nation=nation;
        this.proposer=proposer;
        deadline=System.currentTimeMillis()+1000L*60*60*24*3; // 3 days
        resolutionId=UUID.randomUUID().toString();
    }
    public void propose(){
        if((this instanceof OutsiderResolution) || nation.getRank(proposer).weight() >= 500) {
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
            if(importance()<=3&&!(this instanceof OutsiderResolution)) sign(proposer);
        }else{
            proposer.sendMessage(Message.getMessage("nation.resolution.cannot-propose"));
        }
    }
    // 1=little effect such as rename
    // 2=changing ranks (to&from <500)/ kicking players / inviting players
    // 3=changing ranks/kicking players (500-900)
    // 4=diplomatic ones such as declaring war/changing relation/signing treaty
    // 5=disbanding nation/changing owner/changing type
    public abstract int importance();
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
    public boolean checkPass(){
        return !isExecuted&&nation.getType().decisionMaker.checkPass(this);
    }
    public abstract void execute();
    @Nonnull
    public abstract Message getResolutionContent();
}
