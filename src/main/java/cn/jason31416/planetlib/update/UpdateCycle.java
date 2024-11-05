package cn.jason31416.planetlib.update;

import cn.jason31416.planetlib.PlanetLib;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class UpdateCycle extends BukkitRunnable {
    public static UpdateCycle instance;
    private static final Map<String, UpdateTask> tasks=new java.util.HashMap<>();

    public int tick=0;
    @Override
    public void run() {
        tick ++;
        for(UpdateTask task : tasks.values()) {
            if(tick % task.interval == 0&&!task.isExecuting){
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        task.isExecuting=true;
                        try {
                            task.runnableTask.run();
                        } catch (Exception e) {
                            task.isExecuting = false;
                            throw e;
                        }
                        task.isExecuting = false;
                    }
                }.runTaskAsynchronously(PlanetLib.instance);
            }
        }
    }

    public static void start() {
        if(instance != null) instance.cancel();
        instance = new UpdateCycle();
        instance.runTaskTimer(PlanetLib.instance, 0, 1);
    }
    public static void registerTask(String name, UpdateTask task) {
        if(tasks.containsKey(name)) return;
        tasks.put(name, task);
    }
    public static void setInterval(String name, int interval) {
        if(!tasks.containsKey(name)) return;
        tasks.get(name).interval = interval;
    }
    public static void unregisterTask(String name) {
        tasks.remove(name);
    }
    public static void unregisterAllTasks() {
        tasks.clear();
    }
    public static void stop(){
        instance.cancel();
        unregisterAllTasks();
    }
}
