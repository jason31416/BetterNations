package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.message.StaticMessages;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class YamlStorage extends AbstractStorage {
    File directory;
    public YamlStorage(File directory) {
        this.directory = directory;
    }
    @Override
    public void save() {
        for(DataList<?> dataList : dataLists){
            File file = new File(directory, dataList.getName() + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            for(Object data : dataList.getAllData()){
                DataItem dataItem = new DataItem();
                try {
                    if (dataList.serialize(data, dataItem)) {
                        for (String key : dataItem.data.keySet()) {
                            config.set(dataItem.getUUID() + "." + key, dataItem.data.get(key));
                        }
                    }
                } catch (Exception e) {
                    StaticMessages.FAILED_TO_SAVE_DATA.sendConsole();
                    e.printStackTrace();
                }
            }
            try {
                config.save(file);
            } catch (IOException e) {
                StaticMessages.FAILED_TO_SAVE_DATA.sendConsole();
                e.printStackTrace();
            }
        }
    }

    @Override
    public void load() {
        for(DataList<?> dataList : dataLists){
            File file = new File(directory, dataList.getName() + ".yml");
            if(file.exists()) {
                try {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    for (String key : config.getKeys(false)) {
                        ConfigurationSection section = config.getConfigurationSection(key);
                        if (section == null) continue;
                        DataItem dataItem = new DataItem();
                        dataItem.setUUID(UUID.fromString(key));
                        for (String subKey : section.getKeys(false)) {
                            dataItem.put(subKey, section.get(subKey));
                        }
                        dataList.deserialize(dataItem);
                    }
                } catch (Exception e) {
                    StaticMessages.FAILED_TO_LOAD_DATA.sendConsole();
                    e.printStackTrace();
                }
            }
        }
    }
}
