import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdjustBot {
    public static void main(String[] args) {
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

        Runnable print = () -> {
        };

        service.scheduleAtFixedRate(print, 0, 1, TimeUnit.MINUTES);
    }
}
