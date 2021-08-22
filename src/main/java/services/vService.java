package services;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public interface vService extends Runnable {
    int getRunPeriodSeconds();
}
