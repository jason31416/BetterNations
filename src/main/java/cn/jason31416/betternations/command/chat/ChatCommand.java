package cn.jason31416.betternations.command.chat;

import cn.jason31416.betternations.nation.Town;
import cn.jason31416.betternations.nation.TownRole;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.command.IParentCommand;
import cn.jason31416.planetlib.command.ParentCommand;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageLoader;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ChatCommand extends ParentCommand {
    public ChatCommand(IParentCommand parent) {
        super("chat", parent);
    }

    public static class ChatType {
        public enum Type {
            GLOBAL,
            NATION,
            TOWN
        }
        public ChatType(Type t) {
            type = t;
            town = null;
        }
        public ChatType(String Town) {
            town = Town;
            type = Type.TOWN;
        }
        public Type type;
        public String town;
    }
    public final static ChatType CTGlobal = new ChatType(ChatType.Type.GLOBAL);
    public final static ChatType CTNation = new ChatType(ChatType.Type.NATION);
    public final static Map<String, ChatType> CTTownMap = new HashMap<>();
    public static Map<String, ChatType> playerChatMap = new HashMap<>();

    @Override
    @Nullable
    public Message executeRaw(ICommandContext context) {
        if (context.getArg(0).isEmpty()) return null;
        switch (context.getArg(0)) {
            case "town" -> {
                if (context.getArg(1).isEmpty()) return null;
                Town t = Town.getTown(context.getArg(1));
                if (t == null) {
                    return Message.getMessage("command.failed.town-not-exist");
                }
                if (t.getRole(context.player()) == TownRole.NONE) {
                    return Message.getMessage("command.failed.no-permission");
                }
                if (!CTTownMap.containsKey(context.getArg(1)))
                    CTTownMap.put(context.getArg(1), new ChatType(context.getArg(1)));
                playerChatMap.put(context.player().getName(), CTTownMap.get(context.getArg(1)));
                MessageLoader.getMessage("command.success.chat-changed")
                        .add("chat", Message.getMessage("chats.town")
                                .add("town", context.getArg(1)));
            }
            case "nation" -> {
                playerChatMap.put(context.player().getName(), CTNation);
                MessageLoader.getMessage("command.success.chat-changed")
                        .add("chat", Message.getMessage("chats.nation"));
            }
            case "global" -> {
                playerChatMap.put(context.player().getName(), CTGlobal);
                MessageLoader.getMessage("command.success.chat-changed")
                        .add("chat", Message.getMessage("chats.global"));
            }
            default -> {
                return Message.getMessage("command.failed.invalid-chat-type");
            }
        }
        return null;
    }
}
