package services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class vService implements Runnable {
    public abstract int getRunPeriodSeconds();

    public void start() {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(this, 0, getRunPeriodSeconds(), TimeUnit.SECONDS);
    }
}
