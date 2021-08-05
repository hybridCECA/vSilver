import org.json.JSONException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, JSONException {
        if (args.length == 0) {
            AdjustBot.start();
        } else if (args.length == 1 && args[0].equals("collect")) {
            ProfitabilityCollector.start();
        } else {
            System.err.println("Usage: java -jar [executable].jar [collect (optional)]");
            System.err.println("0 arguments: Runs adjust bot");
            System.err.println("collect argument: Runs profitability collector");
        }

    }
}
