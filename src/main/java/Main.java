import nicehash.NHApi;
import services.*;
import southxchange.SXApi;
import utils.Config;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length == 3) {
            Config.setDatabaseConfig(args[0], args[1], args[2]);
            NHApi.loadConfig();
            SXApi.loadConfig();

            List<vService> services;
            if (System.getenv("VSILVER_DEV_ENVIRONMENT") != null) {
                // Adjust bot may interfere in dev env
                services = List.of(
                        new DataCollector(),
                        new TransferBot(),
                        new MaxProfit()
                );
            } else {
                services = List.of(
                        new AdjustBot(),
                        new DataCollector(),
                        new TransferBot(),
                        new MaxProfit()
                );
            }

            services.forEach(vService::start);
        } else {
            System.err.println("Usage: java -jar [executable].jar [postgres username] [postgres password] [postgres url]");
            System.err.println("Url example: 192.168.86.36:5432/vsilver");
        }

    }
}
