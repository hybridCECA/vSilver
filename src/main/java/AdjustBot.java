import nicehash.Api;
import nicehash.OrderBot;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdjustBot {
    // Check period in seconds
    private static final int CHECK_PERIOD = 30;

    public static void start() throws IOException, JSONException {
        Api.loadConfig();

        List<JSONObject> orderConfigList = Config.getOrderConfigList();
        List<OrderBot> orderBotList = new ArrayList<>();
        for (JSONObject object : orderConfigList) {
            OrderBot bot = new OrderBot(object);
            orderBotList.add(bot);
        }

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        Runnable run = () -> {
            for (int i = 0; i < orderBotList.size(); i++) {
                System.out.println("Order Bot " + i + ":");
                orderBotList.get(i).run();
                System.out.println();
            }
        };

        service.scheduleAtFixedRate(run, 0, CHECK_PERIOD, TimeUnit.SECONDS);
    }
}
