package database;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import test.generated.tables.records.ConfigRecord;
import test.generated.tables.records.PairDataRecord;
import utils.Config;

import java.sql.DriverManager;
import java.sql.SQLException;

import static test.generated.Tables.CONFIG;
import static test.generated.Tables.PAIR_DATA;

public class Connection {
    private static java.sql.Connection getConnection() throws SQLException {
        String userName = Config.getDatabaseUserName();
        String password = Config.getDatabasePassword();
        String url = "jdbc:postgresql://" + Config.getDatabaseUrl();

        return DriverManager.getConnection(url, userName, password);
    }

    public static String getConfigValue(String key) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            ConfigRecord record = create.selectFrom(CONFIG).where(CONFIG.KEY.equal(key)).fetchOne();

            return record.getValue();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Config value not found!");
    }

    public static void putPair(int algoPrice, String algo, String coin, int coinRevenue, double exchangeRate, String market) {
        try (java.sql.Connection conn = getConnection()) {
            DSLContext create = DSL.using(conn, SQLDialect.POSTGRES);

            PairDataRecord pair = create.newRecord(PAIR_DATA);

            pair.setAlgoFulfillPrice(algoPrice);
            pair.setAlgoName(algo);
            pair.setCoinName(coin);
            pair.setCoinRevenue(coinRevenue);
            pair.setExchangeRate(exchangeRate);
            pair.setMarket(market);
            pair.store();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
