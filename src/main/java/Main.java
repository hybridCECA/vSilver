import dataclasses.NicehashAlgorithm;
import dataclasses.NicehashAlgorithmBuyInfo;
import dataclasses.NicehashOrder;
import nicehash.NHApi;
import org.json.JSONException;
import services.*;
import southxchange.SXApi;
import utils.Config;
import utils.SingletonFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static int getNumOrders() throws JSONException {
        NHApi nhApi = SingletonFactory.getInstance(NHApi.class);
        List<NicehashAlgorithm> algoList = nhApi.getAlgoList();

        int count = 0;

        for (NicehashAlgorithm algo : algoList) {
            NicehashAlgorithmBuyInfo buyInfo = nhApi.getAlgoBuyInfo(algo.getAlgorithm());
            for (String market : buyInfo.getMarkets()) {
                List<NicehashOrder> orderbook = nhApi.getOrderbook(algo.getAlgorithm(), market);

                count += orderbook.size();
            }
        }

        return count;
    }

    public static void main(String[] args) {
        if (args.length == 3) {
            Config.setDatabaseConfig(args[0], args[1], args[2]);
            SXApi.loadConfig();

            List<vService> services;
            if (System.getenv("VSILVER_DEV_ENVIRONMENT") != null) {
                // Adjust bot may interfere in dev env

                services = List.of(
                        /*
                        new AdjustBot(),
                        new TransferBot(),
                        MaxProfitFactory.getInstance(),
                        */
                        new DataCollector(),
                        new MiscMaintainer()
                );

                //MarketEvaluation.start();
                //CoinAlgoMatcher.workshop();
            } else {
                services = List.of(
                        new AdjustBot(),
                        new BotSynchronizer(),
                        new DataCollector(),
                        new TransferBot(),
                        new MiscMaintainer(),
                        SingletonFactory.getInstance(MaxProfit.class)
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
