package utils;

import database.Connection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

class ConfigTest {
    @BeforeAll
    static void loadConfig() {
        Config.setDatabaseConfig(System.getenv("database_username"), System.getenv("database_password"), System.getenv("database_url"));
    }

    @Test
    void testGetConfigValue() {
        List<String> configConsts = List.of(
                Consts.ORDER_TARGET_DAYS,
                Consts.ORG_ID,
                Consts.API_KEY,
                Consts.API_SECRET,
                Consts.ADJUST_BOT_PERIOD_SECONDS,
                Consts.DATA_COLLECTOR_PERIOD_SECONDS,
                Consts.ADJUST_BOT_ADJUST_TO_REFRESH_RATIO,
                Consts.MAX_PROFIT_ANALYZE_MINUTES
        );

        for (String config : configConsts) {
            Connection.getConfigValue(config);
        }
    }
}