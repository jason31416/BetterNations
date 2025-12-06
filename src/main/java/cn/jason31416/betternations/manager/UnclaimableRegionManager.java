package cn.jason31416.betternations.manager;

import cn.jason31416.betternations.BetterNations;
import cn.jason31416.planetlib.wrapper.SimpleChunkLocation;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UnclaimableRegionManager {
    public static List<SimpleChunkLocation> unclaimableChunks = new ArrayList<>();
    public static boolean canClaim(SimpleChunkLocation chunk){
        return !unclaimableChunks.contains(chunk);
    }
    @SneakyThrows
    public static void save(){
        if(unclaimableChunks.isEmpty()) return;
        try(FileWriter writer = new FileWriter(new File(BetterNations.instance.getDataFolder(), "unclaimables.json"))){
            writer.write(new Gson().toJson(unclaimableChunks.stream().map(SimpleChunkLocation::serialize).toList()));
        }
    }
    @SneakyThrows
    public static void load() {
        File file = new File(BetterNations.instance.getDataFolder(), "unclaimables.json");
        if (!file.exists()) return;
        try(FileInputStream reader = new FileInputStream(file)){
            List<Map<String, Object>> serializedChunks = new Gson().fromJson(new String(reader.readAllBytes()), new TypeToken<List<Map<String, Object>>>(){}.getType());
            unclaimableChunks = new ArrayList<>(serializedChunks.stream().map(SimpleChunkLocation::deserialize).toList());
        }
    }
}
