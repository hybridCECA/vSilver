import marketevaluation.MarketEvaluation;
import services.*;
import southxchange.SXApi;
import utils.Config;
import utils.SingletonFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java -jar [executable].jar [postgres username] [postgres password] [postgres url]");
            System.err.println("Url example: 192.168.86.36:5432/vsilver");
            System.exit(1);
        }
        if (args.length > 3) {
            System.out.println("Warning: Ignoring extra arguments");
        }

        Config.setDatabaseConfig(args[0], args[1], args[2]);
        SXApi.loadConfig();

        List<vService> services;
        if (System.getenv("VSILVER_DEV_ENVIRONMENT") != null) {
            services = List.of(
            );

            try {
                MarketEvaluation.workshop();
                //CoinAlgoMatcher.workshop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (System.getenv("VSILVER_STAGING_ENVIRONMENT") != null) {
            services = List.of(
                    //new AdjustBot(),
                    new BotSynchronizer(),
                    new DataCollector(),
                    //new TransferBot(),
                    new MiscMaintainer(),
                    new MarketEvaluator(),
                    // Refiller doesn't refill yet, just test for now
                    new Refiller(),
                    SingletonFactory.getInstance(MaxProfit.class)
            );
        } else {
            services = List.of(
                    new AdjustBot(),
                    new BotSynchronizer(),
                    new DataCollector(),
                    new TransferBot(),
                    new MiscMaintainer(),
                    new MarketEvaluator(),
                    new Refiller(),
                    SingletonFactory.getInstance(MaxProfit.class)
            );
        }

        for (vService service : services) {
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(service, 0, service.getRunPeriodSeconds(), TimeUnit.SECONDS);
        }
    }
}
