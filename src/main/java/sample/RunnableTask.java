package sample;

import java.util.TimerTask;

public class RunnableTask extends TimerTask {
    private Runnable runnable;

    public RunnableTask(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        runnable.run();
    }
}
