import MarketEvaluation.MarketEvaluationWorkshop;
import services.*;
import southxchange.SXApi;
import utils.Config;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        if (args.length == 3) {
            Config.setDatabaseConfig(args[0], args[1], args[2]);
            SXApi.loadConfig();

            List<vService> services;
            if (System.getenv("VSILVER_DEV_ENVIRONMENT") != null) {
                // Adjust bot may interfere in dev env

                services = List.of(
                        /*
                        new DataCollector(),
                        new TransferBot(),
                        MaxProfitFactory.getInstance()

                         */
                );

                MarketEvaluationWorkshop.start();
            } else {
                services = List.of(
                        new AdjustBot(),
                        new DataCollector(),
                        new TransferBot(),
                        MaxProfitFactory.getInstance()
                );
            }

            for (vService service : services) {
                ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.scheduleAtFixedRate(service, 0, service.getRunPeriodSeconds(), TimeUnit.SECONDS);
            }
        } else {
            System.err.println("Usage: java -jar [executable].jar [postgres username] [postgres password] [postgres url]");
            System.err.println("Url example: 192.168.86.36:5432/vsilver");
        }

    }
}
