import nicehash.Api;
import services.AdjustBot;
import services.DataCollector;
import utils.Config;

public class Main {
    public static void main(String[] args) {
        if (args.length == 3) {
            Config.setDatabaseConfig(args[0], args[1], args[2]);
            Api.loadConfig();

            AdjustBot.start();
            DataCollector.start();
        } else {
            System.err.println("Usage: java -jar [executable].jar [postgres username] [postgres password] [postgres url]");
            System.err.println("Url example: 192.168.86.36:5432/vsilver");
        }

    }
}
