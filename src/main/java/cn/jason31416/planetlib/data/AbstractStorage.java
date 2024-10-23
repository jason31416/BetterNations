package cn.jason31416.planetlib.data;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStorage {
    public List<DataList<?>> dataLists = new ArrayList<>();
    public void registerDataList(DataList<?> dataList) {
        dataLists.add(dataList);
    }
    public abstract void save();
    public abstract void load();
}
