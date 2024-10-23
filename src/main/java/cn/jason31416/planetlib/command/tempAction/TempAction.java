package cn.jason31416.planetlib.command.tempAction;

import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TempAction {
    public static final Map<String, TempAction> actions = new HashMap<>();
    public static interface Action {
        void execute();
    }
    private final Action action;
    private final SimplePlayer forPlayer;
    private final long startTime;
    private final String uuid;
    public TempAction(Action action, SimplePlayer forPlayer) {
        this.action = action;
        this.forPlayer = forPlayer;
        this.startTime = System.currentTimeMillis();
        this.uuid = UUID.randomUUID().toString();
        actions.put(uuid, this);
    }
    public boolean execute(SimplePlayer player) {
        if (player.equals(forPlayer)) {
            action.execute();
            actions.remove(uuid);
            return true;
        }else{
            return false;
        }
    }
    public static void checkAll(){
        for (String action : new ArrayList<>(actions.keySet())) {
            if (System.currentTimeMillis() - actions.get(action).startTime > 1000 * 60 * 5) { // 5 minutes
                actions.remove(action);
            }
        }
    }
    public static void createAction(Action action, SimplePlayer forPlayer) {
        new TempAction(action, forPlayer);
    }
}
