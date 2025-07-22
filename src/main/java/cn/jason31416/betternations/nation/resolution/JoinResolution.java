package cn.jason31416.betternations.nation.resolution;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nonnull;

public class JoinResolution extends AbstractResolution implements DailyResolution, OutsiderResolution {
    public JoinResolution(Nation nation, SimplePlayer proposer) {
        // Note that proposer is the player who wants to join the nation (not in the nation yet)
        super(nation, proposer);
    }

    @Override
    public void execute() {
        if(proposer.getNation()==null){
            nation.addPlayer(proposer);
            proposer.sendMessage(Message.getMessage("command.success.join-nation").add("nation_name", nation.getName()));
        }
    }

    @Override
    public @Nonnull Message getResolutionContent() {
        return Message.getMessage("nation.resolution.description.join-nation")
                .add("nation", nation.getName())
                .add("requester", proposer.getName());
    }
}
