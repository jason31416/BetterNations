package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.nation.Nation;
import cn.jason31416.planetlib.Utils;
import cn.jason31416.planetlib.data.IDataItem;
import cn.jason31416.planetlib.message.Message;

import java.util.*;

public class HistoricalBroadcastManager {
    public static record HistoricalEvent(UUID id, Integer order, Integer date, String time, String cont, List<String> nations) {
        public boolean serialize(IDataItem dataItem){
            dataItem.setUUID(id);
            dataItem.set("date", date);
            dataItem.set("order", order);
            dataItem.set("time", time);
            dataItem.set("cont", cont);
            dataItem.set("nations", String.join(",", nations));
            return true;
        }
        public static HistoricalEvent deserialize(IDataItem dataItem){
            HistoricalEvent event = new HistoricalEvent(dataItem.getUUID(), dataItem.getInteger("order"), dataItem.getInteger("date"), dataItem.getString("time"), dataItem.getString("cont"), List.of(dataItem.getString("nations").split(",")));
            history.putIfAbsent(event.date(), new ArrayList<>());
            history.get(event.date()).add(event);
            history.get(event.date()).sort(Comparator.comparing(a -> a.order));
            return event;
        }
    }
    public static Map<Integer, List<HistoricalEvent> > history=new HashMap<>();
    public static void broadcast(Message content, List<Nation> nations){
        long time = System.currentTimeMillis();
        int day = (int) (time/1000/60/60/24);
        history.putIfAbsent(day, new ArrayList<>());
        HistoricalEvent event = new HistoricalEvent(UUID.randomUUID(), history.get(day).size(), day, Utils.getTimeString(time).split(" ")[1], content.toFormatted(), nations.stream().map(Nation::getName).toList());
        history.get(day).add(event);
        Message.getMessage("history.broadcast-message").add("content", content).broadcast();
    }
}
