package utmn.checkmates.server.utility;

import utmn.checkmates.server.utility.logger.Logger;

public class Timer implements Runnable{
    private long remainSeconds;
    private ScheduledTask scheduled;

    public Timer(long remainSeconds, ScheduledTask scheduledTask) {
        this.remainSeconds = remainSeconds;
        this.scheduled = scheduledTask;
    }

    @Override
    public void run() {
        Logger.log("Timer", "run", "Запущен таймер на %d секунд".formatted(remainSeconds));

        while(remainSeconds > 0){
            try {
                remainSeconds -= 1;
                Thread.sleep(1_000L);
                Logger.log("Timer", "run", "Осталось: %d секунд".formatted(remainSeconds));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Logger.log("Timer", "run", "Таймер окончен");
        if(scheduled != null) {
            Logger.log("Timer", "run", "Запускается отложенная задача!");
            scheduled.execute();
        }
    }

    @FunctionalInterface
    public interface ScheduledTask {
        void execute() ;
    }
}
