import database.Connection;
import dataclasses.TriplePair;
import datacollector.DataCollector;
import nicehash.Api;
import org.json.JSONException;
import services.MaxProfitService;
import utils.Config;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length == 3) {
            Config.setDatabaseConfig(args[0], args[1], args[2]);
            Api.loadConfig();

            //DataCollector.start();
            MaxProfitService.register(new TriplePair("ZHASH", "EU", "BitcoinGold"));
            MaxProfitService.start();
        } else {
            System.err.println("Usage: java -jar [executable].jar [postgres username] [postgres password] [postgres url]");
            System.err.println("Url example: 192.168.86.36:5432/vsilver");
        }

    }
}
