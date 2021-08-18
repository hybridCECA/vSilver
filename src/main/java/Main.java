import nicehash.Api;
import org.apache.http.client.config.RequestConfig;
import services.AdjustBot;
import services.DataCollector;
import services.TransferBot;
import services.vService;
import southxchange.SXApi;
import utils.Config;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 3) {
            Config.setDatabaseConfig(args[0], args[1], args[2]);
            Api.loadConfig();
            SXApi.loadConfig();

            List<vService> services = List.of(
                    new AdjustBot(),
                    new DataCollector(),
                    new TransferBot()
            );

            services.forEach(vService::start);
        } else {
            System.err.println("Usage: java -jar [executable].jar [postgres username] [postgres password] [postgres url]");
            System.err.println("Url example: 192.168.86.36:5432/vsilver");
        }

    }
}
