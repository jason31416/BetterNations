package cn.jason31416.planetlib.update;

public class UpdateTask {
    public interface RunnableTask {
        void run();
    }
    public final RunnableTask runnableTask;
    public final int interval;
    public boolean isExecuting=false;

    public UpdateTask(int interval, RunnableTask runnableTask) {
        this.runnableTask = runnableTask;
        this.interval = interval;
    }
}
