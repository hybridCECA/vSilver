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
                Consts.ORG_ID,
                Consts.API_KEY,
                Consts.API_SECRET,
                Consts.ADJUST_BOT_PERIOD_SECONDS,
                Consts.DATA_COLLECTOR_PERIOD_SECONDS,
                Consts.MAX_PROFIT_ANALYZE_MINUTES,
                Consts.ORDER_BOT_MIN_PROFIT_MARGIN,
                Consts.SX_API_KEY,
                Consts.SX_API_SECRET,
                Consts.TRANSFER_BOT_PERIOD_SECONDS,
                Consts.CONNECTION_TIMEOUT_MS,
                Consts.MAX_PROFIT_PERIOD_SECONDS,
                Consts.TRANSFER_BOT_BTC_MIN_AMOUNT,
                Consts.BOT_SYNCHRONIZER_PERIOD_SECONDS,
                Consts.CLEAN_DATABASE_DAYS,
                Consts.MISC_MAINTAINER_PERIOD_SECONDS,
                Consts.ALL_DATA_FETCH_STOP,
                Consts.MARKET_EVALUATION_ANALYZE_MINUTES,
                Consts.MARKET_EVALUATOR_PERIOD_SECONDS
        );

        for (String config : configConsts) {
            Connection.getConfigValue(config);
        }
    }
}